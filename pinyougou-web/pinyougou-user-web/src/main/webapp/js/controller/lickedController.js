app.controller('lickedController', function($scope, $controller, $timeout, baseService) {

    $controller('indexController', {$scope: $scope});

    $scope.findAllApplication = function () {
        baseService.sendGet("/licked/findLickers").then(function (response) {
            if (response.data.licker) {
                $scope.myLicker = response.data.licker;
            }
            $scope.applicationList = response.data.lilckerList;
        })
    };

    $scope.acceptLicker = function (lickerId) {
        baseService.sendGet("/licked/acceptLicker?lickerId=" + lickerId).then(function (response) {
            if (response.data) {
                $scope.findAllApplication();
            } else {
                alert("操作失败")
            }
        })
    };

    $scope.refuseLicker = function (lickerId) {
        baseService.sendGet("/licked/refuseLicker?lickerId=" + lickerId).then(function (response) {
            if (response.data) {
                $scope.findAllApplication();
            } else {
                alert("操作失败")
            }
        })
    };

    $scope.deleteLicker = function (lickerId) {
        baseService.sendGet("/licked/deleteLicker?lickerId=" + lickerId).then(function (response) {
            if (response.data) {
                $scope.findAllApplication();
            } else {
                alert("操作失败")
            }
        })
    }
});