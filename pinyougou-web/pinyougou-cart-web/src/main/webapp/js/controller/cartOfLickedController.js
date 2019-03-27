app.controller('cartOfLickedController', function ($scope, $interval, $location, $controller, baseService) {
    $controller('cartController', {$scope: $scope});

    $scope.findYourLickedCart = function () {
        var lickedId = $location.search().lickedId;
        baseService.sendGet("/lickerCart/findYourLickedCart?lickedId=" + lickedId).then(function (response) {
            $scope.cartList = response.data;

            $scope.totalEntity = {totalNum: 0, totalMoney: 0};

            for (var i = 0; i < $scope.cartList.length; i++) {
                var cart = $scope.cartList[i];
                for (var j = 0; j < cart.orderItems.length; j++) {
                    var orderItem = cart.orderItems[j];
                }
            }
        })
    };

    $scope.lickerTempCartForLicked = function () {
        var lickedId = $location.search().lickedId;
        $scope.packIds();
        $scope.packedIds.push(lickedId);

        if ($scope.packedIds.length > 0) {
            baseService.sendGet("/lickerCart/lickerTempCartForLicked?itemIds=" + $scope.packedIds).then(function (response) {
                if (response.data) {
                    location.href = "http://cart.pinyougou.com/order/getYourLickedOrderInfo.html?lickedId=" + lickedId;
                } else {
                    alert("结算失败")
                }
            })
        } else {
            alert("请先选择商品");
        }
    }
});
