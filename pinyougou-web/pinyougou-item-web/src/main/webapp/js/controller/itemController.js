app.controller('itemController', function ($scope, $http) {

    $scope.updateNum = function (x) {
        $scope.num = parseInt($scope.num);
        $scope.num += x;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    };

    $scope.specItems = {};

    $scope.select = function (name, value) {
        $scope.specItems[name] = value;
        $scope.searchSku();
    };

    $scope.isSelected = function (name, value) {
        return $scope.specItems[name] == value;
    };

    $scope.loadSku = function () {
        $scope.sku = itemList[0];

        $scope.specItems = JSON.parse($scope.sku.spec);
    };

    $scope.searchSku = function () {
        for (i = 0; i < itemList.length; i ++) {
            if (itemList[i].spec == JSON.stringify($scope.specItems)) {
                $scope.sku = itemList[i];
                break;
            }
        }
    };

    $scope.addToCart = function () {
        alert("skuId:" + $scope.sku.id);
        $http.get("http://cart.pinyougou.com/cart/addCart?itemId=" + $scope.sku.id +"&num=" + $scope.num, {'withCredentials':true}).then(function (response) {
            if (response.data) {
                location.href = 'http://cart.pinyougou.com/cart.html';
            } else {
                alert("请求失败");
            }
        });
    };

    $scope.showLoginName = function () {
        $http.get("/user/showLoginName").then(function (response) {
            $scope.loginName = response.data.loginName;
            $scope.redirectUrl = window.encodeURIComponent(location.href);
        })
    }

});