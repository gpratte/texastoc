package com.texastoc.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.TopTenPoints;
import com.texastoc.service.PointSystemService;
import com.texastoc.service.calculate.PayoutCalculator;

@Controller
public class PointSystemController extends BaseController {
    
    @Autowired
    PointSystemService pointSystemService;
    @Autowired
    PayoutCalculator payoutCalculator;

    @RequestMapping("/admin/pointsystem")
    public ModelAndView getPointSystem(final HttpServletRequest request,
            @RequestParam(value="min", required=true) Integer min,
            @RequestParam(value="max", required=true) Integer max) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        List<TopTenPoints> topTenPoints = pointSystemService.findTopTenPoints(min, max);
        return new ModelAndView("pointsystem", "topTenPoints", topTenPoints);
    }

    @RequestMapping("/mobile/pointsystem")
    public ModelAndView getMobilePointSystem(final HttpServletRequest request,
            @RequestParam(value="min", required=true) Integer min,
            @RequestParam(value="max", required=true) Integer max) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        List<TopTenPoints> topTenPoints = pointSystemService.findTopTenPoints(min, max);
        return new ModelAndView("mobilepointsystem", "topTenPoints", topTenPoints);
    }

    @RequestMapping("/mobile/payouts")
    public ModelAndView getMobilePayouts(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        HashMap<Integer, HashMap<Integer,Float>> payouts = payoutCalculator.getPayouts();
        return new ModelAndView("mobilepayouts", "payouts", payouts);
    }

}
