package com.notif.api.admin;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import com.notif.api.station.StationService;
import com.notif.common.dto.station.StationLoadView;
import com.notif.common.dto.station.StationPreviewView;

@ExtendWith(MockitoExtension.class)
class AdminStationControllerTest {

    @Mock
    private StationService station;

    @InjectMocks
    private AdminStationController controller;

    @Test
    void previewDelegates() {
        StationPreviewView preview = new StationPreviewView(1, java.util.List.of(), java.util.List.of());
        when(station.preview()).thenReturn(preview);
        assertThat(controller.preview()).isSameAs(preview);
    }

    @Test
    void loadDefaultsToDecide() {
        StationLoadView view = new StationLoadView(5, 13, 0, 65, 7);
        when(station.load(true)).thenReturn(view);
        assertThat(controller.load(true)).isEqualTo(view);
        verify(station).load(true);
    }
}
