app.controller('brandController', function ($scope, $controller, baseService) {

    //继承baseController
    $controller("baseController", {$scope:$scope});

    $scope.searchBrand = {};

    $scope.search = function (page, rows) {
        baseService.findAsPage("/brand/findAsPage", page, rows, $scope.searchBrand)
            .then(function (response) {
                $scope.brands = response.data.rows;
                $scope.paginationConf.totalItems = response.data.total;
            })
    };

    $scope.save = function () {
        var url = "save";
        baseService.sendPost("/brand/save", $scope.brand).then(function (response) {
            if (response.data) {
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    };

    $scope.show = function (brand) {
        $scope.brand = JSON.parse(JSON.stringify(brand));
    };

});