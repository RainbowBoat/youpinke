/** 定义控制器层 */
app.controller('indexController', function($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});

    $scope.getLoginName = function () {
        baseService.sendGet("/seller/getLoginName").then(function (value) {
            $scope.loginName = value.data.loginName;
        });
    };



});