package ru.sber.transport.telemechanic.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "predict-config")
public class PredictProperties {
    private boolean needPlateNumber;
    private boolean needCarWipers;
    private boolean needWasherLiquid;
    private boolean needFrontLights;
    private boolean needBackLights;
    private boolean needSideMirrors;
    private boolean needSplashGuards;
    private boolean needWindScreen;
    private boolean needPowerSteeringLiquidLevel;
    private boolean needCoolantLevel;
    private boolean needOilLevel;
    private boolean needInstrumentPanel;
    private String hostAi;
    private String store;
    private String password;
    private boolean needFileUpload;
}
