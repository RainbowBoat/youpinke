app.controller('orderController', function ($scope, $interval, $location, $controller, baseService) {
    $controller('baseController', {$scope: $scope});

    $scope.findCart = function () {
        baseService.sendGet("/cart/findTempCartFromRedis").then(function (response) {
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
        });
    };


    $scope.findAllAddress = function () {
        baseService.sendGet("/address/findAllAddress").then(function (response) {
            $scope.addressList = response.data;
            $scope.address = $scope.addressList[0];
        })
    };

    $scope.isSelected = function (address) {
        return $scope.address == address;
    };

    $scope.select = function (address) {
        $scope.address = address;
    };

    $scope.order = {paymentType:'1'};

    $scope.changePayment = function (type) {
        $scope.order.paymentType = type;
    };

    $scope.saveOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;
        baseService.sendPost("/order/saveOrder", $scope.order).then(function (response) {
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

    $scope.genPayCode = function () {
        baseService.sendGet("/order/genPayCode").then(function (response) {
            $scope.outTradeNo = response.data.outTradeNo;
            $scope.money = (response.data.totalFee / 100).toFixed(2);
            $scope.codeUrl = response.data.codeUrl;

        document.getElementById("qrious").src = "/barcode?url=" + $scope.codeUrl;
        });

        var timer = $interval(function () {
            baseService.sendGet("/order/queryPayStatus?outTradeNo=" + $scope.outTradeNo).then(function (response) {
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
    };

    $scope.getMoney = function () {
        return $location.search().money;
    };




    /* 购买指定商品 */

    // $scope.findCartBySelected = function () {
    //     $scope.selectedIds = $location.search().packedIds;
    // }


});