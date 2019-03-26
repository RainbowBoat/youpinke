app.controller('lickerOrderController', function ($scope, $interval, $location, $controller, baseService) {
    $controller('orderController', {$scope: $scope});

    $scope.findLickerTempCartForLicked = function () {
        var lickedId = $location.search().lickedId;
        baseService.sendGet("/lickerCart/findLickerTempCartForLicked?lickedId=" + lickedId).then(function (response) {
            $scope.cartList = response.data;

            $scope.totalEntity = {totalNum: 0, totalMoney: 0};

            for (var i = 0; i < $scope.cartList.length; i++) {
                var cart = $scope.cartList[i];
                for (var j = 0; j < cart.orderItems.length; j++) {
                    var orderItem = cart.orderItems[j];
                    $scope.totalEntity.totalNum += orderItem.num;
                    $scope.totalEntity.totalMoney += orderItem.totalFee;
                }
            }
        })
    };

    $scope.findDefaultAddress = function () {
        var lickedId = $location.search().lickedId;
        baseService.sendGet("/lickerCart/findDefaultAddress?lickedId=" + lickedId).then(function (response) {
            $scope.addressList = response.data;
            $scope.address = $scope.addressList[0];
        })
    };

    $scope.saveLickOrder = function () {
        var lickedId = $location.search().lickedId;
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = lickedId;

        baseService.sendPost("/order/saveLickOrder", $scope.order).then(function (response) {
            if (response.data) {
                if ($scope.order.paymentType == 1) {
                    location.href = "/order/pay.html";
                } else {
                    location.href = "/order/paysuccess.html";
                }
            } else {
                alert("提交订单失败")
            }
        })
    };

    $scope.genLickPayCode = function () {
        var lickedId = $location.search().lickedId;
        baseService.sendGet("/order/genLickPayCode?lickedId=" + lickedId).then(function (response) {
            $scope.loutTradeNo = response.data.outTradeNo;
            $scope.money = (response.data.totalFee / 100).toFixed(2);
            $scope.codeUrl = response.data.codeUrl;

            document.getElementById("qrious").src = "/barcode?url=" + $scope.codeUrl;
        });

        var timer = $interval(function () {
            baseService.sendGet("/order/queryLickPayStatus?outTradeNo=" + $scope.outTradeNo + "&lickedId=" + lickedId).then(function (response) {
                $scope.payStatus = response.data.payStatus;
                if ($scope.payStatus == "1") {
                    $interval.cancel(timer);
                    location.href = "/order/paysuccess.html?money=" + $scope.money;
                }
                if ($scope.payStatus == "3") {
                    $interval.cancel(timer);
                    location.href = "/order/payfail.html"
                }

            })
        }, 3000, 180);

        timer.then(function () {
            $scope.payStatus = 4;
        })
    }

});