package com.texastoc.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.texastoc.domain.Game;
import com.texastoc.domain.Season;
import com.texastoc.domain.Supply;
import com.texastoc.domain.SupplyType;
import com.texastoc.service.GameService;
import com.texastoc.service.ProcurementService;
import com.texastoc.service.SeasonService;
import com.texastoc.service.calculate.SeasonCalculator;

@Controller
public class ProcurementController extends BaseController {

    static final Logger logger = Logger.getLogger(ProcurementController.class);

    @Autowired
    private GameService gameService;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private SeasonCalculator seasonCalculator;
    @Autowired
    private ProcurementService procurementService;


    @RequestMapping(value = "/admin/supplies", method = RequestMethod.GET)
    public ModelAndView getAllSupplies(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<Supply> supplies = procurementService.getAllSupplies();
        ModelAndView mav = new ModelAndView("supplies", "supplies", supplies);
        List<Game> games = gameService.findAll();
        mav.addObject("games", games);
        List<Season> seasons = seasonService.findAll();
        mav.addObject("seasons", seasons);
        Integer kitty = procurementService.getKitty();
        mav.addObject("kitty", kitty);
        return mav;
    }

    @RequestMapping(value = "/admin/supplies/new", method = RequestMethod.GET)
    public ModelAndView showNewSupply(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        ModelAndView mav = new ModelAndView("supply");
        mav.addObject("types", SupplyType.values());
        Integer kitty = procurementService.getKitty();
        mav.addObject("kitty", kitty);
        return mav;
    }

    @RequestMapping(value = "/admin/supplies/edit/{id}", method = RequestMethod.GET)
    public ModelAndView editSupply(final HttpServletRequest request,
            @PathVariable("id") Integer id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        Supply supply = procurementService.getSupply(id);
        ModelAndView mav = new ModelAndView("supply");
        mav.addObject("supply", supply);
        mav.addObject("types", SupplyType.values());
        Integer kitty = procurementService.getKitty();
        mav.addObject("kitty", kitty);
        return mav;
    }

    @RequestMapping(value = "/admin/supplies/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteSupply(final HttpServletRequest request,
            @PathVariable("id") Integer id) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        procurementService.delete(id);
        Season season = seasonService.getCurrent();
        try {
            seasonCalculator.calculate(season.getId());
        } catch (Exception e) {
            logger.error(e);
        }
        List<Supply> supplies = procurementService.getAllSupplies();
        ModelAndView mav = new ModelAndView("supplies", "supplies", supplies);
        List<Game> games = gameService.findAll();
        mav.addObject("games", games);
        List<Season> seasons = seasonService.findAll();
        mav.addObject("seasons", seasons);
        Integer kitty = procurementService.getKitty();
        mav.addObject("kitty", kitty);
        return mav;
    }

    @RequestMapping(value = "/admin/supplies/create", method = RequestMethod.POST)
    public ModelAndView createOrUpdateSupply(final HttpServletRequest request,
            @RequestParam(value = "supplyid", required = false) Integer supplyId,
            @RequestParam(value = "supplytype", required = true) String supplyType,
            @RequestParam(value = "tocamount", required = false) String tocAmount,
            @RequestParam(value = "kittyamount", required = false) String kittyAmount,
            @RequestParam(value = "description", required = false) String description) {
        
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<String> errors = new ArrayList<String>();
        if (StringUtils.isBlank(tocAmount) && StringUtils.isBlank(kittyAmount)) {
            errors.add("Either TOC Amount or Kitty Amount must be set");
        }

        if (StringUtils.isNotBlank(tocAmount) && !NumberUtils.isNumber(tocAmount)) {
            errors.add("TOC Amount must be a dollar amount");
        }

        if (StringUtils.isNotBlank(kittyAmount) && !NumberUtils.isNumber(kittyAmount)) {
            errors.add("Kitty Amount must be a dollar amount");
        }

        if (errors.size() == 0) {
            if (supplyId == null) {
                Season currentSeason = seasonService.getCurrent();
                
                Supply supply = new Supply();
                supply.setSeasonId(currentSeason.getId());
                supply.setType(SupplyType.fromString(supplyType));
                if (StringUtils.isNotBlank(tocAmount)) {
                    supply.setAnnualTocAmount(new Integer(tocAmount));
                }
                if (StringUtils.isNotBlank(kittyAmount)) {
                    supply.setKittyAmount(new Integer(kittyAmount));
                }
                supply.setDescription(description);
                procurementService.procure(supply);
            } else {
                Supply supply = procurementService.getSupply(supplyId);
                supply.setType(SupplyType.fromString(supplyType));
                if (StringUtils.isNotBlank(tocAmount)) {
                    supply.setAnnualTocAmount(new Integer(tocAmount));
                } else {
                    supply.setAnnualTocAmount(null);
                }
                if (StringUtils.isNotBlank(kittyAmount)) {
                    supply.setKittyAmount(new Integer(kittyAmount));
                } else {
                    supply.setKittyAmount(null);
                }
                supply.setDescription(description);
                procurementService.update(supply);
            }

            Season season = seasonService.getCurrent();
            try {
                seasonCalculator.calculate(season.getId());
            } catch (Exception e) {
                logger.error(e);
            }

            List<Supply> supplies = procurementService.getAllSupplies();
            ModelAndView mav = new ModelAndView("supplies", "supplies", supplies);
            List<Game> games = gameService.findAll();
            mav.addObject("games", games);
            List<Season> seasons = seasonService.findAll();
            mav.addObject("seasons", seasons);
            Integer kitty = procurementService.getKitty();
            mav.addObject("kitty", kitty);
            return mav;
        } else {
            ModelAndView mav = new ModelAndView("supply");
            mav.addObject("types", SupplyType.values());
            mav.addObject("errors", errors);
            Integer kitty = procurementService.getKitty();
            mav.addObject("kitty", kitty);
            return mav;
        }
    }

    @RequestMapping(value = "/mobile/supplies", method = RequestMethod.GET)
    public ModelAndView mobileSupplies(final HttpServletRequest request) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }

        List<Supply> supplies = procurementService.getAllSupplies();
        ModelAndView mav = new ModelAndView("mobilesupplies", "supplies", supplies);
        List<Game> games = gameService.findAll();
        mav.addObject("games", games);
        List<Season> seasons = seasonService.findAll();
        mav.addObject("seasons", seasons);
        Integer kitty = procurementService.getKitty();
        mav.addObject("kitty", kitty);
        return mav;
    }

    @RequestMapping(value = "/admin/supply/invoice/add/{id}", method = RequestMethod.GET)
    public ModelAndView adminAddInvoice(final HttpServletRequest request,
            @PathVariable("id") Integer supplyId) {
        if (this.isNotLoggedIn(request)) {
            return new ModelAndView("login");
        }
        return new ModelAndView("supplyInvoiceUpload", "supplyId", supplyId);
    }
    
    @RequestMapping(value = "/admin/supply/invoice/upload/{id}", method = RequestMethod.POST)
    public ModelAndView handleFileUpload(@PathVariable("id") Integer supplyId,
            @RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            
            try {
                byte[] invoice = file.getBytes();
                procurementService.addInvoice(supplyId, invoice);
            } catch (IOException e) {
                ModelAndView mav = new ModelAndView("supplyInvoiceUpload", "supplyId",
                        supplyId);
                mav.addObject("error", e.getMessage());
                return mav;
            }
            
            Supply supply = procurementService.getSupply(supplyId);
            ModelAndView mav = new ModelAndView("supply");
            mav.addObject("supply", supply);
            mav.addObject("types", SupplyType.values());
            Integer kitty = procurementService.getKitty();
            mav.addObject("kitty", kitty);
            return mav;
        } else {
            ModelAndView mav = new ModelAndView("supplyInvoiceUpload", "supplyId",
                    supplyId);
            mav.addObject("error", "No file choosen");
            return mav;
        }
    }

    @RequestMapping(value = "/admin/supply/invoice/delete/{id}", method = RequestMethod.GET)
    public ModelAndView deleteSupplyInvoice(@PathVariable("id") Integer supplyId) {

        procurementService.removeInvoice(supplyId);
        Supply supply = procurementService.getSupply(supplyId);
        ModelAndView mav = new ModelAndView("supply");
        mav.addObject("supply", supply);
        mav.addObject("types", SupplyType.values());
        Integer kitty = procurementService.getKitty();
        mav.addObject("kitty", kitty);
        return mav;
    }

    @RequestMapping(value = "/admin/supply/invoice/{id}", method = RequestMethod.GET)
    public void getSupplyInvoice(HttpServletResponse response,
            @PathVariable("id") Integer supplyId) {

        byte[] bytes = procurementService.getInvoice(supplyId);

        try {
            
            File temp = File.createTempFile("" + System.currentTimeMillis(), ".tmp"); 
            FileOutputStream fileOuputStream = new FileOutputStream(temp); 
            fileOuputStream.write(bytes);
            fileOuputStream.close();
            
            InputStream inputStream = new FileInputStream(temp);
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment; filename="+System.currentTimeMillis());
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            inputStream.close();
        } catch (Exception e){
            logger.error("Problem with file download",e);
        }    
    }
}
