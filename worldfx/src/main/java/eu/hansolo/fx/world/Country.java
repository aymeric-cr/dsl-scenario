/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.world;

import javafx.scene.paint.Color;


/**
 * Created by hansolo on 22.11.16.
 */
public enum Country {
    ABW,
    AFG,
    AGO,
    AIA,
    ALB,
    ALD,
    AND,
    ARE,
    ARG,
    ARM,
    ASM,
    ATA,
    ATC,
    ATF,
    ATG,
    AUS,
    AUT,
    AZE,
    BDI,
    BEL,
    BEN,
    BFA,
    BGD,
    BGR,
    BHR,
    BHS,
    BIH,
    BJN,
    BLM,
    BLR,
    BLZ,
    BMU,
    BOL,
    BRA,
    BRB,
    BRN,
    BTN,
    BWA,
    CAF,
    CAN,
    CHE,
    CHL,
    CHN,
    CIV,
    CLP,
    CMR,
    CNM,
    COD,
    COG,
    COK,
    COL,
    COM,
    CPV,
    CRI,
    CSI,
    CUB,
    CUW,
    CYM,
    CYN,
    CYP,
    CZE,
    DEU,
    DJI,
    DMA,
    DNK,
    DOM,
    DZA,
    ECU,
    EGY,
    ERI,
    ESB,
    ESP,
    EST,
    ETH,
    FIN,
    FJI,
    FLK,
    FRA,
    FRO,
    FSM,
    GAB,
    GBR,
    GEO,
    GGY,
    GHA,
    GIB,
    GIN,
    GMB,
    GNB,
    GNQ,
    GRC,
    GRD,
    GRL,
    GTM,
    GUM,
    GUY,
    HKG,
    HMD,
    HND,
    HRV,
    HTI,
    HUN,
    IDN,
    IMN,
    IND,
    IOA,
    IOT,
    IRL,
    IRN,
    IRQ,
    ISL,
    ISR,
    ITA,
    JAM,
    JEY,
    JOR,
    JPN,
    KAB,
    KAS,
    KAZ,
    KEN,
    KGZ,
    KHM,
    KIR,
    KNA,
    KOR,
    KOS,
    KWT,
    LAO,
    LBN,
    LBR,
    LBY,
    LCA,
    LIE,
    LKA,
    LSO,
    LTU,
    LUX,
    LVA,
    MAC,
    MAF,
    MAR,
    MCO,
    MDA,
    MDG,
    MDV,
    MEX,
    MHL,
    MKD,
    MLI,
    MLT,
    MMR,
    MNE,
    MNG,
    MNP,
    MOZ,
    MRT,
    MSR,
    MUS,
    MWI,
    MYS,
    NAM,
    NCL,
    NER,
    NFK,
    NGA,
    NIC,
    NIU,
    NLD,
    NOR,
    NPL,
    NRU,
    NZL,
    OMN,
    PAK,
    PAN,
    PCN,
    PER,
    PGA,
    PHL,
    PLW,
    PNG,
    POL,
    PRI,
    PRK,
    PRT,
    PRY,
    PSX,
    PYF,
    QAT,
    ROU,
    RUS,
    RWA,
    SAH,
    SAU,
    SCR,
    SDN,
    SDS,
    SEN,
    SER,
    SGP,
    SGS,
    SHN,
    SLB,
    SLE,
    SLV,
    SMR,
    SOL,
    SOM,
    SPM,
    SRB,
    STP,
    SUR,
    SVK,
    SVN,
    SWE,
    SWZ,
    SXM,
    SYC,
    SYR,
    TCA,
    TCD,
    TGO,
    THA,
    TJK,
    TKM,
    TLS,
    TON,
    TTO,
    TUN,
    TUR,
    TUV,
    TWN,
    TZA,
    UGA,
    UKR,
    UMI,
    URY,
    USA,
    USG,
    UZB,
    VAT,
    VCT,
    VEN,
    VGB,
    VIR,
    VNM,
    VUT,
    WLF,
    WSB,
    WSM,
    YEM,
    ZAF,
    ZMB,
    ZWE;

    private Color color;


    // ******************** Constructors **************************************
    Country() {
        color = null;
    }


    // ******************** Methods *******************************************
    public String getName() {
        return name();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(final Color COLOR) {
        color = COLOR;
    }
}