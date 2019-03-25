/** 定义秒杀商品控制器 */
app.controller("seckillGoodsController", function($scope,$controller,$location,$timeout,baseService){

    /** 指定继承cartController */
    $controller("baseController", {$scope:$scope});

    $scope.findSeckillGoods = function(){
        baseService.sendGet("/seckill/findSeckillGoods")
            .then(function(response){
                $scope.seckillGoodsList = response.data;
            });
    };

    $scope.findOne = function () {
        var id = $location.search().id;
        baseService.sendGet("/seckill/findOne?id=" + id).then(function (response) {
            if (response.data) {
                $scope.seckillGoods = response.data;
                $scope.downCount($scope.seckillGoods.endTime)
            }

        });
    };

    $scope.submitOrder = function () {
        if ($scope.loginName) {
            baseService.sendGet("/order/submitOrder?id=" + $scope.seckillGoods.id).then(function (response) {
                if (response.data) {
                    location.href = "/order/pay.html"
                } else {
                    alert("下单失败!")
                }
            })
        } else {
            location.href = "http://sso.pinyougou.com?service=" + $scope.redirectUrl;
        }
    };

    $scope.downCount = function (endTime) {
        var milliSeconds = new Date(endTime).getTime() - new Date().getTime();

        var seconds = Math.floor(milliSeconds/1000);

        if (seconds > 0) {
            var minutes = Math.floor(seconds/60);

            var hours = Math.floor(minutes/60);

            var days = Math.floor(hours/24);

            var timeArr = new Array();

            if (days > 0) {
                timeArr.push(calc(days) + "天 ");
            }

            if (hours > 0) {
                timeArr.push(calc(hours-(days*24)) + ":");
            }

            if (minutes > 0) {
                timeArr.push(calc(minutes-(hours*60)) + ":");
            }

            timeArr.push(calc(seconds-(minutes*60)));

            $scope.timeStr = timeArr.join("");

            $timeout(function () {
                $scope.downCount(endTime);
            }, 1000);
        } else {
            $scope.timeStr = "秒杀已结束";
        }
    };

    var calc = function (num) {
        return num > 9 ? num : "0" + num;
    }

});