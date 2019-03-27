package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.Cart;
import com.pinyougou.pojo.Address;
import com.pinyougou.service.AddressService;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/lickerCart")
public class LickerCartController {

    @Autowired
    private HttpServletRequest request;

    @Reference(timeout = 100000)
    private CartService cartService;

    @Reference
    private AddressService addressService;

    @GetMapping("/findYourLickedCart")
    public List<Cart> findYourLickedCart(String lickedId) {
        String lickerId = request.getRemoteUser();

        List<Cart> yourLickedCart = cartService.findYourLickedCart(lickerId, lickedId);

        return yourLickedCart;
    }

    @GetMapping("/findDefaultAddress")
    public List<Address> findDefaultAddress(String lickedId) {
        List<Address> lickedIdAddressList = addressService.findAllByUsername(lickedId);
        List<Address> defaultAddress = new ArrayList<>();
        defaultAddress.add(lickedIdAddressList.get(0));
        return defaultAddress;
    }

    @GetMapping("/lickerTempCartForLicked")
    public boolean lickerTempCartForLicked(String[] itemIds) {
        try {
            String[] newItemIds = Arrays.copyOf(itemIds, itemIds.length - 1);
            String lickedId = itemIds[itemIds.length - 1];
            String lickerId = request.getRemoteUser();
            cartService.lickerTempCartForLicked(lickerId, lickedId, newItemIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/findLickerTempCartForLicked")
    public List<Cart> findLickerTempCartForLicked(String lickedId) {

        List<Cart> lickerTempCartForLickedList = cartService.findLickerTempCartForLicked(lickedId);

        return lickerTempCartForLickedList;
    }
}
