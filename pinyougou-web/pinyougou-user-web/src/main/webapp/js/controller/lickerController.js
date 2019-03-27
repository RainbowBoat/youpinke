app.controller('lickerController', function($scope, $controller, $timeout, baseService) {

    $controller('indexController', {$scope: $scope});

    $scope.findLickeds = function () {
        baseService.sendGet("/licker/findLickeds").then(function (response) {
            $scope.lickedList = response.data;
        })
    };

    $scope.preLicked = {};

    $scope.beALicker = function () {
        baseService.sendPost("/licker/beALicker", $scope.preLicked).then(function (response) {
            if (response.data) {
                alert("已提交舔狗申请, 求求你做个人吧");
            } else {
                alert("提交申请失败")
            }
        })
    };

    $scope.beAMan = function (lickedId) {
        baseService.sendGet("/licker/beAMan?lickedId=" + lickedId).then(function (response) {
            if (response.data) {
                alert("感天动地, 你终于选择做个人了");
                $scope.findLickeds();
            } else {
                alert("提交申请失败")
            }
        })
    };


});