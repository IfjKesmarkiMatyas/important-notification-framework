package com.notif.api.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.notif.api.station.StationService;
import com.notif.common.dto.station.StationLoadView;
import com.notif.common.dto.station.StationPreviewView;

@RestController
@RequestMapping("/api/admin/station")
public class AdminStationController {

    private final StationService station;

    public AdminStationController(StationService station) {
        this.station = station;
    }

    @GetMapping
    public StationPreviewView preview() {
        return station.preview();
    }

    @PostMapping("/load")
    public StationLoadView load(@RequestParam(defaultValue = "true") boolean decide) {
        return station.load(decide);
    }
}
