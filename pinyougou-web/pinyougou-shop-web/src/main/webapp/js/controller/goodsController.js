/** 定义控制器层 */
app.controller('goodsController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/goods/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    /** 添加或修改 */
    $scope.saveOrUpdate = function(){

        $scope.goods.goodsDesc.introduction = editor.html();

        var url = "save";

        /** 发送post请求 */
        baseService.sendPost("/goods/" + url, $scope.goods)
            .then(function(response){
                if (response.data){
                    /** 重新加载数据 */
                    $scope.reload();
                    $scope.goods = {};
                    editor.html('');
                }else{
                    alert("操作失败！");
                }
            });
    };

    /** 显示修改 */
    $scope.show = function(entity){
       /** 把json对象转化成一个新的json对象 */
       $scope.goods = JSON.parse(JSON.stringify(entity));
    };

    /** 批量删除 */
    $scope.delete = function(){
        if ($scope.ids.length > 0){
            baseService.deleteById("/goods/delete", $scope.ids)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                    }else{
                        alert("删除失败！");
                    }
                });
        }else{
            alert("请选择要删除的记录！");
        }
    };


    /**
     * 商品分类选择
     */
    $scope.findItemsByParentId = function (parentId, name) {
        baseService.sendGet("/itemCat/findItemsByParentId?parentId=" + parentId).then(function (value) {
            $scope[name] = value.data;
        });
    };

    $scope.$watch('goods.category1Id', function (newVal, oldVal) {
        if (newVal) {
            $scope.findItemsByParentId(newVal, 'itemList2');
        } else {
            $scope.itemList2 = [];
        }
    });

    $scope.$watch('goods.category2Id', function (newVal, oldVal) {
        if (newVal) {
            $scope.findItemsByParentId(newVal, 'itemList3');
        } else {
            $scope.itemList3 = [];
        }
    });

    $scope.$watch('goods.category3Id', function (newVal, oldVal) {
        if (newVal) {
            for (var i = 0; i < $scope.itemList3.length; i++) {
                var itemCat = $scope.itemList3[i];
                if (newVal == itemCat.id) {
                    $scope.goods.typeTemplateId = itemCat.typeId;
                    break;
                }
            }
        } else {
            $scope.goods.typeTemplateId = null;
        }
    });

    $scope.$watch('goods.typeTemplateId', function (newVal, oldVal) {
        if (newVal) {
            baseService.sendGet("/template/findOne?id=" + newVal).then(function (value) {
                $scope.brandList = JSON.parse(value.data.brandIds);
                $scope.goods.goodsDesc.customAttributeItems = JSON.parse(value.data.customAttributeItems);
            });

            baseService.sendGet("/template/findSpecByTemplateId?id=" + newVal).then(function (value) {
                $scope.specList = value.data;
                $scope.optionList = [];
            })
        }
    });

    $scope.goods = {goodsDesc : {itemImages:[], specificationItems :[]}};

    $scope.uploadPicture = function () {
        baseService.uploadFile().then(function (value) {
           if (value.status == 200) {
               $scope.picEntity.url = value.data.url;
           }
        });
    };

    $scope.addPic = function () {
        $scope.goods.goodsDesc.itemImages.push($scope.picEntity);
    };

    $scope.deletePicture = function (idx) {
        $scope.goods.goodsDesc.itemImages.splice(idx, 1);
    };
                                              //机身内存  //32G
    $scope.updateSpecAttr = function ($event, specName, optionName) {
        var obj = $scope.searchJsonByKey($scope.goods.goodsDesc.specificationItems, 'attributeName', specName);
        if (obj) {
            if ($event.target.checked) {
                obj.attributeValue.push(optionName);
            } else {
                obj.attributeValue.splice(obj.attributeValue.indexOf(optionName), 1);
                if (obj.attributeValue.length == 0) {
                    $scope.goods.goodsDesc.specificationItems.splice($scope.goods.goodsDesc.specificationItems.indexOf(obj), 1);
                }
            }
        } else {
            $scope.goods.goodsDesc.specificationItems.push({'attributeName':specName, 'attributeValue':[optionName]});
        }
    };

    $scope.searchJsonByKey = function (jsonArr, key, value) {
        for (var i = 0; i < jsonArr.length; i++) {
            if (jsonArr[i][key] == value) {
                return jsonArr[i];
            }
        }
    };

    $scope.createItems = function () {
        $scope.goods.items = [{spec:{}, price:0, num:9999, status:'0', isDefault:'0'}];

        var specItems = $scope.goods.goodsDesc.specificationItems;

        for (var i = 0; i < specItems.length; i++) {
            $scope.goods.items = $scope.swapItems($scope.goods.items, specItems[i].attributeName, specItems[i].attributeValue);
        }
    };

    $scope.swapItems = function (items, attributeName, attributeValues) {
        var newItems = [];

        for (var i = 0; i < items.length; i++) {
            var item = items[i];

            for (var j = 0; j < attributeValues.length; j++) {
                var newItem = JSON.parse(JSON.stringify(item));

                newItem.spec[attributeName] = attributeValues[j];
                newItems.push(newItem)
            }
        }

        return newItems;
    };

    $scope.goodsStatus = ['未审核','已审核','审核未通过','关闭'];

    $scope.goodsSearch = {};

    $scope.search = function (pageNum, rows) {
        baseService.findAsPage("/goods/findAsPage", pageNum, rows, $scope.goodsSearch).then(function (value) {
            $scope.goodsList = value.data.rows;
            $scope.paginationConf.totalItems = value.data.total;
        });
    };

    $scope.changStatus = function (url) {
        url = "/goods/" + url;

        baseService.sendGet(url + "?ids=" + $scope.ids).then(function (value) {
            if (value) {
                $scope.ids = [];
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    }

});