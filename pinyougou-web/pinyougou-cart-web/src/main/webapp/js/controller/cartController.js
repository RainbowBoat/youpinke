app.controller('cartController', function ($scope, $controller, baseService) {
    $controller('baseController', {$scope: $scope});

    $scope.findCart = function () {
        baseService.sendGet("/cart/findCart").then(function (response) {
            $scope.cartList = response.data;

            $scope.totalEntity = {totalNum: 0, totalMoney: 0};

            for (var i = 0; i < $scope.cartList.length; i++) {
                var cart = $scope.cartList[i];
                for (var j = 0; j < cart.orderItems.length; j++) {
                    var orderItem = cart.orderItems[j];

                }
            }

        });
    };

    $scope.updateNum = function (itemId, num) {
        baseService.sendGet("/cart/addCart?itemId=" + itemId + "&num=" + num).then(function (response) {
            if (response.data) {
                $scope.findCart();
            }
        });
    };

    $scope.init = function () {
        $scope.showLoginName();
        $scope.findCart();

    };

    // 更新选中的商品的总价和数量
    $scope.updateTotalMoney = function () {
        $scope.totalEntity.totalNum = 0;
        $scope.totalEntity.totalMoney = 0;
        for (var i = 0; i < $scope.ids.length; i++) {
            var cart = $scope.ids[i];
            // i 就是对应cartList中的购物车的索引
            for (var j = 0; j < cart.length; j++) {
                // 得到ids中该位置的itemId
                var id = cart[j];
                for (var k = 0; k < $scope.cartList[i].orderItems.length; k++) {
                    var orderItem = $scope.cartList[i].orderItems[k];
                    if (orderItem.itemId == id) {
                        $scope.totalEntity.totalNum += orderItem.num;
                        $scope.totalEntity.totalMoney += orderItem.totalFee;
                    }
                }
            }
        }
    };

    /* 全选方法开始 */

    // 将用户选中的id打包
    $scope.packIds = function () {
        $scope.packedIds = [];

        for (var i = 0; i < $scope.ids.length; i++) {
            var cart = $scope.ids[i];
            for (var j = 0; j < cart.length; j++) {
                var itemId = cart[j];
                $scope.packedIds.push(itemId);
            }
        }
    };

    window.onload = function (ev) {
        $scope.completedArr();
    };

    // 根据购物车数组中的购物车数量遍历生成对应长度的数组
    $scope.completedArr = function () {
        // 定义存储用户选中id的数组
        $scope.ids = [];
        // 定义判断商品是否选中的数组
        $scope.checkedItem = [];
        // 定义判断全选数组
        $scope.checkCart = [];

        $scope.initArr($scope.ids);
        $scope.initArr($scope.checkedItem);
        $scope.initCartArr($scope.checkCart);
        $scope.checkAll = false;
    };

    $scope.initArr = function (arr) {
        for (var i = 0; i < $scope.cartList.length; i++) {
            var newArr = [];
            arr.push(newArr);
        }
    };

    $scope.initCartArr = function (cartArr) {
        for (var i = 0; i < $scope.cartList.length; i++) {
            cartArr.push(false);
        }
    };

    // 添加用户选中的商品
    $scope.updateSelection = function ($event, id, cartIndex, itemIndex) {
        //判断checkbox是否选中
        if ($event.target.checked) {
            $scope.ids[cartIndex].push(id);
        } else {
            //没有选中
            var ids = $scope.ids[cartIndex].indexOf(id);
            $scope.ids[cartIndex].splice(ids, 1);
        }

        $scope.checkedItem[cartIndex][itemIndex] = $event.target.checked;

        // 判断这个购物车是否选中
        $scope.checkCart[cartIndex] = $scope.cartList[cartIndex].orderItems.length == $scope.ids[cartIndex].length;

        // 判断是否全选
        $scope.ifCheckAll();

        // 更新选中的商品的总价和数量
        $scope.updateTotalMoney();
    };

    // 选中一个购物车
    $scope.selectCart = function (cartIndex, $event) {
        // 清空对应的购物车
        $scope.ids[cartIndex] = [];

        // 判断购物车的每个元素是否选中
        for (var i = 0; i < $scope.cartList[cartIndex].orderItems.length; i++) {
            $scope.checkedItem[cartIndex][i] = $event.target.checked;

            if ($event.target.checked) {
                $scope.ids[cartIndex].push($scope.cartList[cartIndex].orderItems[i].itemId);
            }
        }

        // 判断这个购物车是否选中
        $scope.checkCart[cartIndex] = $scope.cartList[cartIndex].orderItems.length == $scope.ids[cartIndex].length;

        // 判断是否全选
        $scope.ifCheckAll();

        // 更新选中的商品的总价和数量
        $scope.updateTotalMoney();
    };

    $scope.ifCheckAll = function () {
        $scope.checkAll = true;
        for (var i = 0; i < $scope.checkCart.length; i++) {
            if (!$scope.checkCart[i]) {
                $scope.checkAll = false;
            }
        }
    };

    // 全选按钮
    $scope.checkAllButton = function ($event) {
        for (var i = 0; i < $scope.checkCart.length; i++) {
            $scope.selectCart(i, $event);
        }

        // 更新选中的商品的总价和数量
        $scope.updateTotalMoney();
    };

    $scope.pushIds = function () {

        $scope.packIds();

        if ($scope.packedIds.length > 0) {
            baseService.sendGet("/cart/tempCart?itemIds=" + $scope.packedIds).then(function (response) {
                if (response.data) {
                    location.href = "/order/getOrderInfo.html";
                } else {
                    alert("结算失败")
                }
            })
        } else {
            alert("请先选择商品");
        }
    };

    /* 全选方法结束 */


    /* 让他人清空购物车 开始 */

    $scope.letOthers2Pay = function () {

        $scope.packIds();

        if ($scope.packedIds.length > 0) {
            baseService.sendGet("/lickedCart/letOthers2Pay?itemIds=" + $scope.packedIds).then(function (response) {
                if (response.data) {
                    alert("已经通知您的舔狗")
                } else {
                    alert("结算失败")
                }
            })
        } else {
            alert("请先选择商品");
        }
    };

    /* 让他人清空购物车 结束 */

});