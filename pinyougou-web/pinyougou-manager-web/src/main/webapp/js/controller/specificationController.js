app.controller("specificationController", function ($scope, $controller, baseService) {

    $controller("baseController", {$scope:$scope});

    $scope.searchSpec = {};

    $scope.search = function (pageNum, rows) {
        baseService.findAsPage("/spec/findAsPage", pageNum, rows, $scope.searchSpec).then(function (response) {
            $scope.dataList = response.data.rows;
            $scope.paginationConf.totalItems = response.data.total;
        })
    };

    $scope.addTableRow = function () {
        $scope.specification.specificationOptions.push({});
    };

    $scope.deleteTableRow = function (idx) {
        $scope.specification.specificationOptions.splice(idx, 1);
    };

    $scope.save = function () {

        var url = "save";
        if ($scope.specification.id) {
            url = "update"
        };

        baseService.sendPost("/spec/" + url, $scope.specification).then(function (value) {
            if (value) {
                $scope.reload();
            } else {
                alert("操作失败!");
            }
        });
    };

    $scope.show = function (specification) {
        $scope.specification = JSON.parse(JSON.stringify(specification));
        // baseService.sendGet("/specOption/findBySpId?specId=" + specification.id).then(function (response) {
        //     $scope.specification.specificationOptions = response.data;
        // })
    };

});