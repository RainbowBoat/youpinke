/** 定义搜索控制器 */
app.controller("searchController", function ($scope, $http, $sce, $location, baseService) {

    $scope.searchParam = {keywords: '', category: '', brand: '', spec: {}, price: '', page: 1, rows: 5, sortField:'', sortValue:''};

    $scope.search = function () {
        baseService.sendPost("/Search", $scope.searchParam).then(function (response) {
            $scope.resultMap = response.data;
            $scope.searchKeyword = $scope.searchParam.keywords;
            $scope.initPageNum();
        });
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

            if ($scope.searchParam.page <= 3) {

                lastPage = 5;
            } else if ($scope.searchParam.page >= totalPages - 2) {
                firstPage = totalPages - 4;
            } else {
                firstPage = $scope.searchParam.page - 2;
                lastPage = $scope.searchParam.page + 2;
            }
        }

        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageNums.push(i);
        };
    };

    $scope.trustHtml = function (htmlStr) {
        return $sce.trustAsHtml(htmlStr);
    };

    $scope.addSearchItem = function (key, value) {

        if (key == ('category') || key == ('brand') || key == ('price')) {
            $scope.searchParam[key] = value;
        } else {
            $scope.searchParam.spec[key] = value;
        }

        $scope.search();
    };

    $scope.removeSearchParams = function (key) {
        if (key == ('category') || key == ('brand') || key == ('price')) {
            $scope.searchParam[key] = '';
        } else {
            delete $scope.searchParam.spec[key];
        }

        $scope.search();
    };

    $scope.searchNum = function (page) {
        page = parseInt(page);

        if (page != $scope.searchParam.page && page >= 1 && page <= $scope.resultMap.totalPages) {
            $scope.searchParam.page = page;
            $scope.search();
        }
    };

    $scope.setSort = function (sortField, sortVale) {
        $scope.searchParam.page = 1;
        $scope.searchParam.sortField = sortField;
        $scope.searchParam.sortValue = sortVale;

        $scope.search();
    };

    $scope.getKeywords = function () {
        $scope.searchParam.keywords = $location.search().keywords;
        $scope.search();
    };


    $scope.showLoginName = function () {
        baseService.sendGet("/user/showLoginName").then(function (response) {
            $scope.loginName = response.data.loginName;
            $scope.redirectUrl = window.encodeURIComponent(location.href);
        })
    };

    $scope.addToCart = function (itemId) {
        $http.get("http://cart.pinyougou.com/cart/addCart?itemId=" + itemId +"&num=" + 1, {'withCredentials':true}).then(function (response) {
            if (response.data) {
                location.href = 'http://cart.pinyougou.com/cart.html';
            } else {
                alert("请求失败");
            }
        });
    };

});
