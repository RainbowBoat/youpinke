app.controller('historyOrderController', function($scope, $controller, $timeout, baseService) {

    $controller('indexController', {$scope: $scope});

    $scope.pageParam = {page:1, rows:5};

    $scope.findOrders = function () {
        baseService.sendPost("/historyOrder/findOrders", $scope.pageParam).then(function (response) {
            $scope.resultMap = response.data;
            $scope.initPageNum();
        })
    };

    $scope.initPageNum = function () {
        $scope.pageNums = [];

        var totalPages = $scope.resultMap.totalPages;
        // alert($scope.resultMap.totalPages);

        var firstPage = 1;

        var lastPage = totalPages;

        // alert(firstPage);
        // alert(lastPage);
        if (totalPages > 5) {

            if ($scope.pageParam.page <= 3) {

                lastPage = 5;
            } else if ($scope.pageParam.page >= totalPages - 2) {
                firstPage = totalPages - 4;
            } else {
                firstPage = $scope.pageParam.page - 2;
                lastPage = $scope.pageParam.page + 2;
            }
        }

        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageNums.push(i);
        };
    };

    // 改变页码
    $scope.searchNum = function (page) {
        page = parseInt(page);

        if (page != $scope.pageParam.page && page >= 1 && page <= $scope.resultMap.totalPages) {
            $scope.pageParam.page = page;
            $scope.findOrders();
        }
    };





});