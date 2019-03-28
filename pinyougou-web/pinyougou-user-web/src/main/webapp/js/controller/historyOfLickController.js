app.controller('historyOfLickController', function($scope, $controller, $location, $timeout, baseService) {

    $controller('historyOrderController', {$scope: $scope});

    $scope.pageParam = {page:1, rows:5};

    $scope.getHistoryOfLick = function () {
        var lickedId = $location.search().lickedId;
        $scope.pageParam.lickedId = lickedId;
        baseService.sendPost("/historyOfLick/GetHistoryOfLick", $scope.pageParam).then(function (response) {
            $scope.resultMap = response.data;
            $scope.initPageNum();
        })
    }


});
