app.controller("typeTemplateController", function ($scope, $controller, baseService) {

    $controller("baseController", {$scope:$scope});

    $scope.searchTemplate = {};

    $scope.search = function (pageNum, rows) {
        baseService.findAsPage("/template/findAsPage", pageNum, rows, $scope.searchTemplate).then(function (repsonse) {
            $scope.templates = repsonse.data.rows;
            $scope.paginationConf.totalItems = repsonse.data.total;
        });
    };

    $scope.findBrandList = function () {
        baseService.sendGet("/brand/findByIdAndName").then(function (reponse) {
            $scope.brandList = {data:reponse.data};
        })
    };

    $scope.findSpecificationList = function () {
        baseService.sendGet("/spec/findByIdAndName").then(function (response) {
            $scope.specificationList = {data:response.data};
        })
    };

    $scope.addAttrRow = function () {
        $scope.template.customAttributeItems.push({});
    };

    $scope.saveOrUpdate = function () {
        var url = "save";
        if ($scope.template.id) {
            url = "update";
        }
        baseService.sendPost("/template/" + url, $scope.template).then(function (response) {
            if (response) {
                $scope.reload();
            } else {
                alert("操作失败");
            }
        });
    };

    $scope.show = function (template) {
        $scope.template = JSON.parse(JSON.stringify(template));
        $scope.template.brandIds = JSON.parse(template.brandIds);
        /** 转换规格列表 */
        $scope.template.specIds = JSON.parse(template.specIds);
        /** 转换扩展属性 */
        $scope.template.customAttributeItems = JSON
            .parse(template.customAttributeItems);
    }

});