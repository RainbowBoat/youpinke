/** 定义控制器层 */
app.controller('sellerController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    $scope.saveOrUpdate = function () {
        var url = "/save";
        if ($scope.id) {
            url = "/update"
        }
        baseService.sendPost("/seller" + url, $scope.seller).then(function (value) {
            if (value.data) {
                location.href = "/shoplogin.html";
            } else {
                alert("操作失败");
            }
        })
    }

});