/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public enum MapcodeTerritory {
    USA(409, "USA", null, new String[]{"US"}, new String[]{"United States of America","America"}),
    IND(406, "India", null, new String[]{"IN"}),
    CAN(494, "Canada", null, new String[]{"CA"}),
    AUS(407, "Australia", null, new String[]{"AU"}),
    MEX(410, "Mexico", null, new String[]{"MX"}),
    BRA(408, "Brazil", null, new String[]{"BR"}),
    RUS(495, "Russia", null, new String[]{"RU"}),
    CHN(527, "China", null, new String[]{"CN"}),
    ATA(539, "Antarctica"),
    VAT(0, "Vatican City", null, null, new String[]{"Holy See)"}),
    MCO(1, "Monaco"),
    GIB(2, "Gibraltar"),
    TKL(3, "Tokelau"),
    CCK(4, "Cocos Islands", null, new String[]{"AU-CC", "AUS-CC"}, new String[]{"Keeling Islands"}),
    BLM(5, "Saint-Barthelemy"),
    NRU(6, "Nauru"),
    TUV(7, "Tuvalu"),
    MAC(8, "Macau", null, new String[]{"CN-92", "CHN-92", "CN-MC", "CHN-MC"}),
    SXM(9, "Sint Maarten"),
    MAF(10, "Saint-Martin"),
    NFK(11, "Norfolk and Philip Island",null, new String[]{"AU-NI", "AUS-NI", "AU-NF", "AUS-NF"}, new String[]{"Philip Island"}),
    PCN(12, "Pitcairn Islands"),
    BVT(13, "Bouvet Island"),
    BMU(14, "Bermuda"),
    IOT(15, "British Indian Ocean Territory", null, new String[]{"DGA"}),
    SMR(16, "San Marino"),
    GGY(17, "Guernsey"),
    AIA(18, "Anguilla"),
    MSR(19, "Montserrat"),
    JEY(20, "Jersey"),
    CXR(21, "Christmas Island", null, new String[]{"AU-CX", "AUS-CX"}),
    WLF(22, "Wallis and Futuna", null, null, new String[]{"Futuna"}),
    VGB(23, "British Virgin Islands", null, null, new String[]{"Virgin Islands, British"}),
    LIE(24, "Liechtenstein"),
    ABW(25, "Aruba"),
    MHL(26, "Marshall Islands", null, new String[]{"WAK"}),
    ASM(27, "American Samoa", null, new String[]{"US-AS", "USA-AS"}, new String[]{"Samoa, American"}),
    COK(28, "Cook islands"),
    SPM(29, "Saint Pierre and Miquelon", null, null, new String[]{"Miquelon"}),
    NIU(30, "Niue"),
    KNA(31, "Saint Kitts and Nevis", null, null, new String[]{"Nevis"}),
    CYM(32, "Cayman islands"),
    BES(33, "Bonaire, St Eustasuis and Saba", null, null, new String[]{"Saba" , "St Eustasius"}),
    MDV(34, "Maldives"),
    SHN(35, "Saint Helena, Ascension and Tristan da Cunha", null, new String[]{"TAA", "ASC"}, new String[]{"Ascension", "Tristan da Cunha"}),
    MLT(36, "Malta"),
    GRD(37, "Grenada"),
    VIR(38, "US Virgin Islands", null, new String[]{"US-VI", "USA-VI"}, new String[]{"Virgin Islands, US"}),
    MYT(39, "Mayotte"),
    SJM(40, "Svalbard and Jan Mayen", null, null, new String[]{"Jan Mayen"}),
    VCT(41, "Saint Vincent and the Grenadines", null, null, new String[]{"Grenadines"}),
    HMD(42, "Heard Island and McDonald Islands", null, new String[]{"AU-HM", "AUS-HM"}, new String[]{"McDonald Islands"}),
    BRB(43, "Barbados"),
    ATG(44, "Antigua and Barbuda", null, null, new String[]{"Barbuda"}),
    CUW(45, "Curacao"),
    SYC(46, "Seychelles"),
    PLW(47, "Palau"),
    MNP(48, "Northern Mariana Islands", null, new String[]{"US-MP", "USA-MP"}),
    AND(49, "Andorra"),
    GUM(50, "Guam", null, new String[]{"US-GU", "USA-GU"}),
    IMN(51, "Isle of Man"),
    LCA(52, "Saint Lucia"),
    FSM(53, "Micronesia", null, null, new String[]{"Federated States of Micronesia"}),
    SGP(54, "Singapore"),
    TON(55, "Tonga"),
    DMA(56, "Dominica"),
    BHR(57, "Bahrain"),
    KIR(58, "Kiribati"),
    TCA(59, "Turks and Caicos Islands", null, null, new String[]{"Caicos Islands"}),
    STP(60, "Sao Tome and Principe", null, null, new String[]{"Principe"}),
    HKG(61, "Hong Kong", null, new String[]{"CN-91", "CHN-91", "CN-HK", "CHN-HK"}),
    MTQ(62, "Martinique"),
    FRO(63, "Faroe Islands"),
    GLP(64, "Guadeloupe"),
    COM(65, "Comoros"),
    MUS(66, "Mauritius"),
    REU(67, "Reunion"),
    LUX(68, "Luxembourg"),
    WSM(69, "Samoa"),
    SGS(70, "South Georgia and the South Sandwich Islands", null, null, new String[]{"South Sandwich Islands"}),
    PYF(71, "French Polynesia"),
    CPV(72, "Cape Verde"),
    TTO(73, "Trinidad and Tobago", null, null, new String[]{"Tobago"}),
    BRN(74, "Brunei"),
    ATF(75, "French Southern and Antarctic Lands"),
    PRI(76, "Puerto Rico", null, new String[]{"US-PR", "USA-PR"}),
    CYP(77, "Cyprus"),
    LBN(78, "Lebanon"),
    JAM(79, "Jamaica"),
    GMB(80, "Gambia"),
    QAT(81, "Qatar"),
    FLK(82, "Falkland Islands"),
    VUT(83, "Vanuatu"),
    MNE(84, "Montenegro"),
    BHS(85, "Bahamas"),
    TLS(86, "East Timor"),
    SWZ(87, "Swaziland"),
    KWT(88, "Kuwait"),
    FJI(89, "Fiji Islands"),
    NCL(90, "New Caledonia"),
    SVN(91, "Slovenia"),
    ISR(92, "Israel"),
    PSE(93, "Palestinian territory"),
    SLV(94, "El Salvador"),
    BLZ(95, "Belize"),
    DJI(96, "Djibouti"),
    MKD(97, "Macedonia"),
    RWA(98, "Rwanda"),
    HTI(99, "Haiti"),
    BDI(100, "Burundi"),
    GNQ(101, "Equatorial Guinea"),
    ALB(102, "Albania"),
    SLB(103, "Solomon Islands"),
    ARM(104, "Armenia"),
    LSO(105, "Lesotho"),
    BEL(106, "Belgium"),
    MDA(107, "Moldova"),
    GNB(108, "Guinea-Bissau"),
    TWN(109, "Taiwan", null, new String[]{"CN-71", "CHN-71", "CN-TW", "CHN-TW"}),
    BTN(110, "Bhutan"),
    CHE(111, "Switzerland"),
    NLD(112, "Netherlands"),
    DNK(113, "Denmark"),
    EST(114, "Estonia"),
    DOM(115, "Dominican Republic"),
    SVK(116, "Slovakia"),
    CRI(117, "Costa Rica"),
    BIH(118, "Bosnia and Herzegovina"),
    HRV(119, "Croatia"),
    TGO(120, "Togo"),
    LVA(121, "Latvia"),
    LTU(122, "Lithuania"),
    LKA(123, "Sri Lanka"),
    GEO(124, "Georgia"),
    IRL(125, "Ireland"),
    SLE(126, "Sierra Leone"),
    PAN(127, "Panama"),
    CZE(128, "Czech Republic"),
    GUF(129, "French Guiana"),
    ARE(130, "United Arab Emirates"),
    AUT(131, "Austria"),
    AZE(132, "Azerbaijan"),
    SRB(133, "Serbia"),
    JOR(134, "Jordan"),
    PRT(135, "Portugal"),
    HUN(136, "Hungary"),
    KOR(137, "South Korea"),
    ISL(138, "Iceland"),
    GTM(139, "Guatemala"),
    CUB(140, "Cuba"),
    BGR(141, "Bulgaria"),
    LBR(142, "Liberia"),
    HND(143, "Honduras"),
    BEN(144, "Benin"),
    ERI(145, "Eritrea"),
    MWI(146, "Malawi"),
    PRK(147, "North Korea"),
    NIC(148, "Nicaragua"),
    GRC(149, "Greece"),
    TJK(150, "Tajikistan"),
    BGD(151, "Bangladesh"),
    NPL(152, "Nepal"),
    TUN(153, "Tunisia"),
    SUR(154, "Suriname"),
    URY(155, "Uruguay"),
    KHM(156, "Cambodia"),
    SYR(157, "Syria"),
    SEN(158, "Senegal"),
    KGZ(159, "Kyrgyzstan"),
    BLR(160, "Belarus"),
    GUY(161, "Guyana"),
    LAO(162, "Laos"),
    ROU(163, "Romania"),
    GHA(164, "Ghana"),
    UGA(165, "Uganda"),
    GBR(166, "United Kingdom", null, null, new String[]{"Scotland","Great Britain","Northern Ireland","Ireland, Northern"}),
    GIN(167, "Guinea"),
    ECU(168, "Ecuador"),
    ESH(169, "Western Sahara", null, null, new String[]{"Sahrawi"}),
    GAB(170, "Gabon"),
    NZL(171, "New Zealand"),
    BFA(172, "Burkina Faso"),
    PHL(173, "Philippines"),
    ITA(174, "Italy"),
    OMN(175, "Oman"),
    POL(176, "Poland"),
    CIV(177, "Ivory Coast"),
    NOR(178, "Norway"),
    MYS(179, "Malaysia"),
    VNM(180, "Vietnam"),
    FIN(181, "Finland"),
    COG(182, "Congo-Brazzaville"),
    DEU(183, "Germany"),
    JPN(184, "Japan"),
    ZWE(185, "Zimbabwe"),
    PRY(186, "Paraguay"),
    IRQ(187, "Iraq"),
    MAR(188, "Morocco"),
    UZB(189, "Uzbekistan"),
    SWE(190, "Sweden"),
    PNG(191, "Papua New Guinea"),
    CMR(192, "Cameroon"),
    TKM(193, "Turkmenistan"),
    ESP(194, "Spain"),
    THA(195, "Thailand"),
    YEM(196, "Yemen"),
    FRA(197, "France"),
    ALA(198, "Aaland Islands"),
    KEN(199, "Kenya"),
    BWA(200, "Botswana"),
    MDG(201, "Madagascar"),
    UKR(202, "Ukraine"),
    SSD(203, "South Sudan"),
    CAF(204, "Central African Republic"),
    SOM(205, "Somalia"),
    AFG(206, "Afghanistan"),
    MMR(207, "Myanmar", null, null, new String[]{"Burma"}),
    ZMB(208, "Zambia"),
    CHL(209, "Chile"),
    TUR(210, "Turkey"),
    PAK(211, "Pakistan"),
    MOZ(212, "Mozambique"),
    NAM(213, "Namibia"),
    VEN(214, "Venezuela"),
    NGA(215, "Nigeria"),
    TZA(216, "Tanzania", null, new String[]{"EAZ"}),
    EGY(217, "Egypt"),
    MRT(218, "Mauritania"),
    BOL(219, "Bolivia"),
    ETH(220, "Ethiopia"),
    COL(221, "Colombia"),
    ZAF(222, "South Africa"),
    MLI(223, "Mali"),
    AGO(224, "Angola"),
    NER(225, "Niger"),
    TCD(226, "Chad"),
    PER(227, "Peru"),
    MNG(228, "Mongolia"),
    IRN(229, "Iran"),
    LBY(230, "Libya"),
    SDN(231, "Sudan"),
    IDN(232, "Indonesia"),
    MX_DIF(233, "Federal District", MapcodeParentTerritory.MEX, new String[]{"MX-DF"}),
    MX_TLA(234, "Tlaxcala", MapcodeParentTerritory.MEX, new String[]{"MX-TL"}),
    MX_MOR(235, "Morelos", MapcodeParentTerritory.MEX, new String[]{"MX-MO"}),
    MX_AGU(236, "Aguascalientes", MapcodeParentTerritory.MEX, new String[]{"MX-AG"}),
    MX_CL(237, "Colima", MapcodeParentTerritory.MEX, new String[]{"MX-COL"}),
    MX_QUE(238, "Queretaro", MapcodeParentTerritory.MEX, new String[]{"MX-QE"}),
    MX_HID(239, "Hidalgo", MapcodeParentTerritory.MEX, new String[]{"MX-HG"}),
    MX_MX(240, "Mexico State", MapcodeParentTerritory.MEX, new String[]{"MX-ME", "MX-MEX"}),
    MX_TAB(241, "Tabasco", MapcodeParentTerritory.MEX, new String[]{"MX-TB"}),
    MX_NAY(242, "Nayarit", MapcodeParentTerritory.MEX, new String[]{"MX-NA"}),
    MX_GUA(243, "Guanajuato", MapcodeParentTerritory.MEX, new String[]{"MX-GT"}),
    MX_PUE(244, "Puebla", MapcodeParentTerritory.MEX, new String[]{"MX-PB"}),
    MX_YUC(245, "Yucatan", MapcodeParentTerritory.MEX, new String[]{"MX-YU"}),
    MX_ROO(246, "Quintana Roo", MapcodeParentTerritory.MEX, new String[]{"MX-QR"}),
    MX_SIN(247, "Sinaloa", MapcodeParentTerritory.MEX, new String[]{"MX-SI"}),
    MX_CAM(248, "Campeche", MapcodeParentTerritory.MEX, new String[]{"MX-CM"}),
    MX_MIC(249, "Michoacan", MapcodeParentTerritory.MEX, new String[]{"MX-MI"}),
    MX_SLP(250, "San Luis Potosi", MapcodeParentTerritory.MEX, new String[]{"MX-SL"}),
    MX_GRO(251, "Guerrero", MapcodeParentTerritory.MEX, new String[]{"MX-GR"}),
    MX_NLE(252, "Nuevo Leon", MapcodeParentTerritory.MEX, new String[]{"MX-NL"}),
    MX_BCN(253, "Baja California", MapcodeParentTerritory.MEX, new String[]{"MX-BC"}),
    MX_VER(254, "Veracruz", MapcodeParentTerritory.MEX, new String[]{"MX-VE"}),
    MX_CHP(255, "Chiapas", MapcodeParentTerritory.MEX, new String[]{"MX-CS"}),
    MX_BCS(256, "Baja California Sur", MapcodeParentTerritory.MEX, new String[]{"MX-BS"}),
    MX_ZAC(257, "Zacatecas", MapcodeParentTerritory.MEX, new String[]{"MX-ZA"}),
    MX_JAL(258, "Jalisco", MapcodeParentTerritory.MEX, new String[]{"MX-JA"}),
    MX_TAM(259, "Tamaulipas", MapcodeParentTerritory.MEX, new String[]{"MX-TM"}),
    MX_OAX(260, "Oaxaca", MapcodeParentTerritory.MEX, new String[]{"MX-OA"}),
    MX_DUR(261, "Durango", MapcodeParentTerritory.MEX, new String[]{"MX-DG"}),
    MX_COA(262, "Coahuila", MapcodeParentTerritory.MEX, new String[]{"MX-CO"}),
    MX_SON(263, "Sonora", MapcodeParentTerritory.MEX, new String[]{"MX-SO"}),
    MX_CHH(264, "Chihuahua", MapcodeParentTerritory.MEX, new String[]{"MX-CH"}),
    GRL(265, "Greenland"),
    SAU(266, "Saudi Arabia"),
    COD(267, "Congo-Kinshasa"),
    DZA(268, "Algeria"),
    KAZ(269, "Kazakhstan"),
    ARG(270, "Argentina"),
    IN_DD(271, "Daman and Diu", MapcodeParentTerritory.IND),
    IN_DN(272, "Dadra and Nagar Haveli", MapcodeParentTerritory.IND),
    IN_CH(273, "Chandigarh", MapcodeParentTerritory.IND),
    IN_AN(274, "Andaman and Nicobar", MapcodeParentTerritory.IND),
    IN_LD(275, "Lakshadweep", MapcodeParentTerritory.IND),
    IN_DL(276, "Delhi", MapcodeParentTerritory.IND),
    IN_ML(277, "Meghalaya", MapcodeParentTerritory.IND),
    IN_NL(278, "Nagaland", MapcodeParentTerritory.IND),
    IN_MN(279, "Manipur", MapcodeParentTerritory.IND),
    IN_TR(280, "Tripura", MapcodeParentTerritory.IND),
    IN_MZ(281, "Mizoram", MapcodeParentTerritory.IND),
    IN_SK(282, "Sikkim", MapcodeParentTerritory.IND, new String[]{"IN-SKM"}),
    IN_PB(283, "Punjab", MapcodeParentTerritory.IND),
    IN_HR(284, "Haryana", MapcodeParentTerritory.IND),
    IN_AR(285, "Arunachal Pradesh", MapcodeParentTerritory.IND),
    IN_AS(286, "Assam", MapcodeParentTerritory.IND),
    IN_BR(287, "Bihar", MapcodeParentTerritory.IND),
    IN_UT(288, "Uttarakhand", MapcodeParentTerritory.IND, new String[]{"IN-UK"}),
    IN_GA(289, "Goa", MapcodeParentTerritory.IND),
    IN_KL(290, "Kerala", MapcodeParentTerritory.IND),
    IN_TN(291, "Tamil Nuda", MapcodeParentTerritory.IND),
    IN_HP(292, "Himachal Pradesh", MapcodeParentTerritory.IND),
    IN_JK(293, "Jammu and Kashmir", MapcodeParentTerritory.IND),
    IN_CT(294, "Chhattisgarh", MapcodeParentTerritory.IND, new String[]{"IN-CG"}),
    IN_JH(295, "Jharkhand", MapcodeParentTerritory.IND),
    IN_KA(296, "Karnataka", MapcodeParentTerritory.IND),
    IN_RJ(297, "Rajasthan", MapcodeParentTerritory.IND),
    IN_OR(298, "Odisha", MapcodeParentTerritory.IND, new String[]{"IN-OD"}, new String[]{"Orissa"}),
    IN_GJ(299, "Gujarat", MapcodeParentTerritory.IND),
    IN_WB(300, "West Bengal", MapcodeParentTerritory.IND),
    IN_MP(301, "Madhya Pradesh", MapcodeParentTerritory.IND),
    IN_AP(302, "Andhra Pradesh", MapcodeParentTerritory.IND),
    IN_MH(303, "Maharashtra", MapcodeParentTerritory.IND),
    IN_UP(304, "Uttar Pradesh", MapcodeParentTerritory.IND),
    IN_PY(305, "Puducherry", MapcodeParentTerritory.IND),
    AU_NSW(306, "New South Wales", MapcodeParentTerritory.AUS),
    AU_ACT(307, "Australian Capital Territory", MapcodeParentTerritory.AUS),
    AU_JBT(308, "Jervis Bay Territory", MapcodeParentTerritory.AUS, new String[]{"AU-JB"}),
    AU_NT(309, "Northern Territory", MapcodeParentTerritory.AUS),
    AU_SA(310, "South Australia", MapcodeParentTerritory.AUS),
    AU_TAS(311, "Tasmania", MapcodeParentTerritory.AUS, new String[]{"AU-TS"}),
    AU_VIC(312, "Victoria", MapcodeParentTerritory.AUS),
    AU_WA(313, "Western Australia", MapcodeParentTerritory.AUS),
    AU_QLD(314, "Queensland", MapcodeParentTerritory.AUS, new String[]{"AU-QL"}),
    BR_DF(315, "Distrito Federal", MapcodeParentTerritory.BRA),
    BR_SE(316, "Sergipe", MapcodeParentTerritory.BRA),
    BR_AL(317, "Alagoas", MapcodeParentTerritory.BRA),
    BR_RJ(318, "Rio de Janeiro", MapcodeParentTerritory.BRA),
    BR_ES(319, "Espirito Santo", MapcodeParentTerritory.BRA),
    BR_RN(320, "Rio Grande do Norte", MapcodeParentTerritory.BRA),
    BR_PB(321, "Paraiba", MapcodeParentTerritory.BRA),
    BR_SC(322, "Santa Catarina", MapcodeParentTerritory.BRA),
    BR_PE(323, "Pernambuco", MapcodeParentTerritory.BRA),
    BR_AP(324, "Amapa", MapcodeParentTerritory.BRA),
    BR_CE(325, "Ceara", MapcodeParentTerritory.BRA),
    BR_AC(326, "Acre", MapcodeParentTerritory.BRA),
    BR_PR(327, "Parana", MapcodeParentTerritory.BRA),
    BR_RR(328, "Roraima", MapcodeParentTerritory.BRA),
    BR_RO(329, "Rondonia", MapcodeParentTerritory.BRA),
    BR_SP(330, "Sao Paulo", MapcodeParentTerritory.BRA),
    BR_PI(331, "Piaui", MapcodeParentTerritory.BRA),
    BR_TO(332, "Tocantins", MapcodeParentTerritory.BRA),
    BR_RS(333, "Rio Grande do Sul", MapcodeParentTerritory.BRA),
    BR_MA(334, "Maranhao", MapcodeParentTerritory.BRA),
    BR_GO(335, "Goias", MapcodeParentTerritory.BRA),
    BR_MS(336, "Mato Grosso do Sul", MapcodeParentTerritory.BRA),
    BR_BA(337, "Bahia", MapcodeParentTerritory.BRA),
    BR_MG(338, "Minas Gerais", MapcodeParentTerritory.BRA),
    BR_MT(339, "Mato Grosso", MapcodeParentTerritory.BRA),
    BR_PA(340, "Para", MapcodeParentTerritory.BRA),
    BR_AM(341, "Amazonas", MapcodeParentTerritory.BRA),
    US_DC(342, "District of Columbia", MapcodeParentTerritory.USA),
    US_RI(343, "Rhode Island", MapcodeParentTerritory.USA),
    US_DE(344, "Delaware", MapcodeParentTerritory.USA),
    US_CT(345, "Connecticut", MapcodeParentTerritory.USA),
    US_NJ(346, "New Jersey", MapcodeParentTerritory.USA),
    US_NH(347, "New Hampshire", MapcodeParentTerritory.USA),
    US_VT(348, "Vermont", MapcodeParentTerritory.USA),
    US_MA(349, "Massachusetts", MapcodeParentTerritory.USA),
    US_HI(350, "Hawaii", MapcodeParentTerritory.USA, new String[]{"US-MID"}),
    US_MD(351, "Maryland", MapcodeParentTerritory.USA),
    US_WV(352, "West Virginia", MapcodeParentTerritory.USA),
    US_SC(353, "South Carolina", MapcodeParentTerritory.USA),
    US_ME(354, "Maine", MapcodeParentTerritory.USA),
    US_IN(355, "Indiana", MapcodeParentTerritory.USA),
    US_KY(356, "Kentucky", MapcodeParentTerritory.USA),
    US_TN(357, "Tennessee", MapcodeParentTerritory.USA),
    US_VA(358, "Virginia", MapcodeParentTerritory.USA),
    US_OH(359, "Ohio", MapcodeParentTerritory.USA),
    US_PA(360, "Pennsylvania", MapcodeParentTerritory.USA),
    US_MS(361, "Mississippi", MapcodeParentTerritory.USA),
    US_LA(362, "Louisiana", MapcodeParentTerritory.USA),
    US_AL(363, "Alabama", MapcodeParentTerritory.USA),
    US_AR(364, "Arkansas", MapcodeParentTerritory.USA),
    US_NC(365, "North Carolina", MapcodeParentTerritory.USA),
    US_NY(366, "New York", MapcodeParentTerritory.USA),
    US_IA(367, "Iowa", MapcodeParentTerritory.USA),
    US_IL(368, "Illinois", MapcodeParentTerritory.USA),
    US_GA(369, "Georgia", MapcodeParentTerritory.USA),
    US_WI(370, "Wisconsin", MapcodeParentTerritory.USA),
    US_FL(371, "Florida", MapcodeParentTerritory.USA),
    US_MO(372, "Missouri", MapcodeParentTerritory.USA),
    US_OK(373, "Oklahoma", MapcodeParentTerritory.USA),
    US_ND(374, "North Dakota", MapcodeParentTerritory.USA),
    US_WA(375, "Washington", MapcodeParentTerritory.USA),
    US_SD(376, "South Dakota", MapcodeParentTerritory.USA),
    US_NE(377, "Nebraska", MapcodeParentTerritory.USA),
    US_KS(378, "Kansas", MapcodeParentTerritory.USA),
    US_ID(379, "Idaho", MapcodeParentTerritory.USA),
    US_UT(380, "Utah", MapcodeParentTerritory.USA),
    US_MN(381, "Minnesota", MapcodeParentTerritory.USA),
    US_MI(382, "Michigan", MapcodeParentTerritory.USA),
    US_WY(383, "Wyoming", MapcodeParentTerritory.USA),
    US_OR(384, "Oregon", MapcodeParentTerritory.USA),
    US_CO(385, "Colorado", MapcodeParentTerritory.USA),
    US_NV(386, "Nevada", MapcodeParentTerritory.USA),
    US_AZ(387, "Arizona", MapcodeParentTerritory.USA),
    US_NM(388, "New Mexico", MapcodeParentTerritory.USA),
    US_MT(389, "Montana", MapcodeParentTerritory.USA),
    US_CA(390, "California", MapcodeParentTerritory.USA),
    US_TX(391, "Texas", MapcodeParentTerritory.USA),
    US_AK(392, "Alaska", MapcodeParentTerritory.USA),
    CA_BC(393, "British Columbia", MapcodeParentTerritory.CAN),
    CA_AB(394, "Alberta", MapcodeParentTerritory.CAN),
    CA_ON(395, "Ontario", MapcodeParentTerritory.CAN),
    CA_QC(396, "Quebec", MapcodeParentTerritory.CAN),
    CA_SK(397, "Saskatchewan", MapcodeParentTerritory.CAN),
    CA_MB(398, "Manitoba", MapcodeParentTerritory.CAN),
    CA_NL(399, "Newfoundland", MapcodeParentTerritory.CAN),
    CA_NB(400, "New Brunswick", MapcodeParentTerritory.CAN),
    CA_NS(401, "Nova Scotia", MapcodeParentTerritory.CAN),
    CA_PE(402, "Prince Edward Island", MapcodeParentTerritory.CAN),
    CA_YT(403, "Yukon", MapcodeParentTerritory.CAN),
    CA_NT(404, "Northwest Territories", MapcodeParentTerritory.CAN),
    CA_NU(405, "Nunavut", MapcodeParentTerritory.CAN),
    RU_MOW(411, "Moscow", MapcodeParentTerritory.RUS),
    RU_SPE(412, "Saint Petersburg", MapcodeParentTerritory.RUS),
    RU_KGD(413, "Kaliningrad Oblast", MapcodeParentTerritory.RUS),
    RU_IN(414, "Ingushetia Republic", MapcodeParentTerritory.RUS),
    RU_AD(415, "Adygea Republic", MapcodeParentTerritory.RUS),
    RU_SE(416, "North Ossetia-Alania Republic", MapcodeParentTerritory.RUS),
    RU_KB(417, "Kabardino-Balkar Republic", MapcodeParentTerritory.RUS),
    RU_KC(418, "Karachay-Cherkess Republic", MapcodeParentTerritory.RUS),
    RU_CE(419, "Chechen Republic", MapcodeParentTerritory.RUS),
    RU_CU(420, "Chuvash Republic", MapcodeParentTerritory.RUS),
    RU_IVA(421, "Ivanovo Oblast", MapcodeParentTerritory.RUS),
    RU_LIP(422, "Lipetsk Oblast", MapcodeParentTerritory.RUS),
    RU_ORL(423, "Oryol Oblast", MapcodeParentTerritory.RUS),
    RU_TUL(424, "Tula Oblast", MapcodeParentTerritory.RUS),
    RU_BE(425, "Belgorod Oblast", MapcodeParentTerritory.RUS, new String[]{"RU-BEL"}),
    RU_VLA(426, "Vladimir Oblast", MapcodeParentTerritory.RUS),
    RU_KRS(427, "Kursk Oblast", MapcodeParentTerritory.RUS),
    RU_KLU(428, "Kaluga Oblast", MapcodeParentTerritory.RUS),
    RU_TT(429, "Tambov Oblast", MapcodeParentTerritory.RUS, new String[]{"RU-TAM"}),
    RU_BRY(430, "Bryansk Oblast", MapcodeParentTerritory.RUS),
    RU_YAR(431, "Yaroslavl Oblast", MapcodeParentTerritory.RUS),
    RU_RYA(432, "Ryazan Oblast", MapcodeParentTerritory.RUS),
    RU_AST(433, "Astrakhan Oblast", MapcodeParentTerritory.RUS),
    RU_MOS(434, "Moscow Oblast", MapcodeParentTerritory.RUS),
    RU_SMO(435, "Smolensk Oblast", MapcodeParentTerritory.RUS),
    RU_DA(436, "Dagestan Republic", MapcodeParentTerritory.RUS),
    RU_VOR(437, "Voronezh Oblast", MapcodeParentTerritory.RUS),
    RU_NGR(438, "Novgorod Oblast", MapcodeParentTerritory.RUS),
    RU_PSK(439, "Pskov Oblast", MapcodeParentTerritory.RUS),
    RU_KOS(440, "Kostroma Oblast", MapcodeParentTerritory.RUS),
    RU_STA(441, "Stavropol Krai", MapcodeParentTerritory.RUS),
    RU_KDA(442, "Krasnodar Krai", MapcodeParentTerritory.RUS),
    RU_KL(443, "Kalmykia Republic", MapcodeParentTerritory.RUS),
    RU_TVE(444, "Tver Oblast", MapcodeParentTerritory.RUS),
    RU_LEN(445, "Leningrad Oblast", MapcodeParentTerritory.RUS),
    RU_ROS(446, "Rostov Oblast", MapcodeParentTerritory.RUS),
    RU_VGG(447, "Volgograd Oblast", MapcodeParentTerritory.RUS),
    RU_VLG(448, "Vologda Oblast", MapcodeParentTerritory.RUS),
    RU_MUR(449, "Murmansk Oblast", MapcodeParentTerritory.RUS),
    RU_KR(450, "Karelia Republic", MapcodeParentTerritory.RUS),
    RU_NEN(451, "Nenets Autonomous Okrug", MapcodeParentTerritory.RUS),
    RU_KO(452, "Komi Republic", MapcodeParentTerritory.RUS),
    RU_ARK(453, "Arkhangelsk Oblast", MapcodeParentTerritory.RUS),
    RU_MO(454, "Mordovia Republic", MapcodeParentTerritory.RUS),
    RU_NIZ(455, "Nizhny Novgorod Oblast", MapcodeParentTerritory.RUS),
    RU_PNZ(456, "Penza Oblast", MapcodeParentTerritory.RUS),
    RU_KI(457, "Kirov Oblast", MapcodeParentTerritory.RUS, new String[]{"RU-KIR"}),
    RU_ME(458, "Mari El Republic", MapcodeParentTerritory.RUS),
    RU_ORE(459, "Orenburg Oblast", MapcodeParentTerritory.RUS),
    RU_ULY(460, "Ulyanovsk Oblast", MapcodeParentTerritory.RUS),
    RU_PM(461, "Perm Krai", MapcodeParentTerritory.RUS, new String[]{"RU-PER"}),
    RU_BA(462, "Bashkortostan Republic", MapcodeParentTerritory.RUS),
    RU_UD(463, "Udmurt Republic", MapcodeParentTerritory.RUS),
    RU_TA(464, "Tatarstan Republic", MapcodeParentTerritory.RUS),
    RU_SAM(465, "Samara Oblast", MapcodeParentTerritory.RUS),
    RU_SAR(466, "Saratov Oblast", MapcodeParentTerritory.RUS),
    RU_YAN(467, "Yamalo-Nenets", MapcodeParentTerritory.RUS),
    RU_KM(468, "Khanty-Mansi", MapcodeParentTerritory.RUS, new String[]{"RU-KHM"}),
    RU_SVE(469, "Sverdlovsk Oblast", MapcodeParentTerritory.RUS),
    RU_TYU(470, "Tyumen Oblast", MapcodeParentTerritory.RUS),
    RU_KGN(471, "Kurgan Oblast", MapcodeParentTerritory.RUS),
    RU_CH(472, "Chelyabinsk Oblast", MapcodeParentTerritory.RUS, new String[]{"RU-CHE"}),
    RU_BU(473, "Buryatia Republic", MapcodeParentTerritory.RUS),
    RU_ZAB(474, "Zabaykalsky Krai", MapcodeParentTerritory.RUS),
    RU_IRK(475, "Irkutsk Oblast", MapcodeParentTerritory.RUS),
    RU_NVS(476, "Novosibirsk Oblast", MapcodeParentTerritory.RUS),
    RU_TOM(477, "Tomsk Oblast", MapcodeParentTerritory.RUS),
    RU_OMS(478, "Omsk Oblast", MapcodeParentTerritory.RUS),
    RU_KK(479, "Khakassia Republic", MapcodeParentTerritory.RUS),
    RU_KEM(480, "Kemerovo Oblast", MapcodeParentTerritory.RUS),
    RU_AL(481, "Altai Republic", MapcodeParentTerritory.RUS),
    RU_ALT(482, "Altai Krai", MapcodeParentTerritory.RUS),
    RU_TY(483, "Tuva Republic", MapcodeParentTerritory.RUS),
    RU_KYA(484, "Krasnoyarsk Krai", MapcodeParentTerritory.RUS),
    RU_MAG(485, "Magadan Oblast", MapcodeParentTerritory.RUS),
    RU_CHU(486, "Chukotka Okrug", MapcodeParentTerritory.RUS),
    RU_KAM(487, "Kamchatka Krai", MapcodeParentTerritory.RUS),
    RU_SAK(488, "Sakhalin Oblast", MapcodeParentTerritory.RUS),
    RU_PO(489, "Primorsky Krai", MapcodeParentTerritory.RUS, new String[]{"RU-PRI"}),
    RU_YEV(490, "Jewish Autonomous Oblast", MapcodeParentTerritory.RUS),
    RU_KHA(491, "Khabarovsk Krai", MapcodeParentTerritory.RUS),
    RU_AMU(492, "Amur Oblast", MapcodeParentTerritory.RUS),
    RU_SA(493, "Sakha Republic", MapcodeParentTerritory.RUS, null, new String[]{"Yakutia Republic"}),
    CN_SH(496, "Shanghai", MapcodeParentTerritory.CHN, new String[]{"CN-31"}),
    CN_TJ(497, "Tianjin", MapcodeParentTerritory.CHN, new String[]{"CN-12"}),
    CN_BJ(498, "Beijing", MapcodeParentTerritory.CHN, new String[]{"CN-11"}),
    CN_HI(499, "Hainan", MapcodeParentTerritory.CHN, new String[]{"CN-46"}),
    CN_NX(500, "Ningxia Hui", MapcodeParentTerritory.CHN, new String[]{"CN-64"}),
    CN_CQ(501, "Chongqing", MapcodeParentTerritory.CHN, new String[]{"CN-50"}),
    CN_ZJ(502, "Zhejiang", MapcodeParentTerritory.CHN, new String[]{"CN-33"}),
    CN_JS(503, "Jiangsu", MapcodeParentTerritory.CHN, new String[]{"CN-32"}),
    CN_FJ(504, "Fujian", MapcodeParentTerritory.CHN, new String[]{"CN-35"}),
    CN_AH(505, "Anhui", MapcodeParentTerritory.CHN, new String[]{"CN-34"}),
    CN_LN(506, "Liaoning", MapcodeParentTerritory.CHN, new String[]{"CN-21"}),
    CN_SD(507, "Shandong", MapcodeParentTerritory.CHN, new String[]{"CN-37"}),
    CN_SX(508, "Shanxi", MapcodeParentTerritory.CHN, new String[]{"CN-14"}),
    CN_JX(509, "Jiangxi", MapcodeParentTerritory.CHN, new String[]{"CN-36"}),
    CN_HA(510, "Henan", MapcodeParentTerritory.CHN, new String[]{"CN-41"}),
    CN_GZ(511, "Guizhou", MapcodeParentTerritory.CHN, new String[]{"CN-52"}),
    CN_GD(512, "Guangdong", MapcodeParentTerritory.CHN, new String[]{"CN-44"}),
    CN_HB(513, "Hubei", MapcodeParentTerritory.CHN, new String[]{"CN-42"}),
    CN_JL(514, "Jilin", MapcodeParentTerritory.CHN, new String[]{"CN-22"}),
    CN_HE(515, "Hebei", MapcodeParentTerritory.CHN, new String[]{"CN-13"}),
    CN_SN(516, "Shaanxi", MapcodeParentTerritory.CHN, new String[]{"CN-61"}),
    CN_NM(517, "Nei Mongol", MapcodeParentTerritory.CHN, new String[]{"CN-15"}, new String []{"Inner Mongolia"}),
    CN_HL(518, "Heilongjiang", MapcodeParentTerritory.CHN, new String[]{"CN-23"}),
    CN_HN(519, "Hunan", MapcodeParentTerritory.CHN, new String[]{"CN-43"}),
    CN_GX(520, "Guangxi Zhuang", MapcodeParentTerritory.CHN, new String[]{"CN-45"}),
    CN_SC(521, "Sichuan", MapcodeParentTerritory.CHN, new String[]{"CN-51"}),
    CN_YN(522, "Yunnan", MapcodeParentTerritory.CHN, new String[]{"CN-53"}),
    CN_XZ(523, "Xizang", MapcodeParentTerritory.CHN, new String[]{"CN-54"}, new String[]{"Tibet"}),
    CN_GS(524, "Gansu", MapcodeParentTerritory.CHN, new String[]{"CN-62"}),
    CN_QH(525, "Qinghai", MapcodeParentTerritory.CHN, new String[]{"CN-63"}),
    CN_XJ(526, "Xinjiang Uyghur", MapcodeParentTerritory.CHN, new String[]{"CN-65"}),
    UMI(528, "United States Minor Outlying Islands", null, new String[]{"US-UM", "USA-UM", "JTN"}),
    CPT(529, "Clipperton Island"),
    AT0(530, "Macquarie Island", MapcodeParentTerritory.ATA),
    AT1(531, "Ross Dependency", MapcodeParentTerritory.ATA),
    AT2(532, "Adelie Land", MapcodeParentTerritory.ATA),
    AT3(533, "Australian Antarctic Territory", MapcodeParentTerritory.ATA),
    AT4(534, "Queen Maud Land", MapcodeParentTerritory.ATA),
    AT5(535, "British Antarctic Territory", MapcodeParentTerritory.ATA),
    AT6(536, "Chile Antartica", MapcodeParentTerritory.ATA),
    AT7(537, "Argentine Antarctica", MapcodeParentTerritory.ATA),
    AT8(538, "Peter 1 Island", MapcodeParentTerritory.ATA),
    AAA(540, "International", null, null, new String[]{"Worldwide","Earth"});
    
    public enum CodeFormat {
        INTERNATIONAL, MINIMAL_UNAMBIGUOUS, MINIMAL
    }

    private final int territoryCode;
    public int getTerritoryCode() {
        return territoryCode;
    }
    
    private final String fullName; 
    public String getFullName() {
        return fullName;
    }

    private final MapcodeTerritory parent;
    public MapcodeTerritory getParent() {
        return parent;
    }

    private final String aliases[];

    private MapcodeTerritory(int territoryCode, String fullName) {
        this(territoryCode, fullName, null, null, null);
    }

    private MapcodeTerritory(int territoryCode, String fullName, MapcodeParentTerritory parent) {
        this(territoryCode, fullName, parent, null, null);
    }
    
    private MapcodeTerritory(int territoryCode, String fullName, MapcodeParentTerritory parent, String[] aliases) {
        this(territoryCode, fullName, parent, aliases, null);
    }
    
    private MapcodeTerritory(int territoryCode, String fullName, MapcodeParentTerritory parent, String[] aliases, String[] fullNameAliases) {
        this.territoryCode = territoryCode;
        this.fullName = fullName;
        if (parent != null) {
            this.parent = parent.toMapCodeTerritory();
        } else {
            this.parent = null;
        }
        if (aliases != null) {
            this.aliases = aliases;
        } else {
            this.aliases = new String[]{};
        }
    }


    private static ArrayList<MapcodeTerritory> codeList;
    private static Map<String, ArrayList<MapcodeTerritory>> nameMap;
    private static ArrayList<MapcodeTerritory> parentList;

    static {
        codeList = new ArrayList<MapcodeTerritory>();
        nameMap = new HashMap<String, ArrayList<MapcodeTerritory>>();
        parentList = new ArrayList<MapcodeTerritory>();

        for (MapcodeTerritory territory : MapcodeTerritory.values()) {
            for (int i = codeList.size(); i <= territory.territoryCode; i++) {
                codeList.add(null);
            }
            codeList.set(territory.territoryCode, territory);
            if (territory.parent != null && !parentList.contains(territory.parent))
            {
                parentList.add(territory.parent);
            }
            addNameWithParentVariants(territory.toString(), territory);
            for (String alias : territory.aliases) {
                addNameWithParentVariants(alias, territory);
            }
        }
    }

    private static void addNameWithParentVariants(String name, MapcodeTerritory mapcodeTerritory) {
        // Add the name as provided
        addNameWithSeperatorVariants(name, mapcodeTerritory);

        if (name.contains("-")) {
            String childTerritoryName = name.substring(name.indexOf("-") + 1);

            // Tolerate a child territory specified without the parent.
            // (e.g. "CA" rather than "US-CA")
            addName(childTerritoryName, mapcodeTerritory);

            if (null != mapcodeTerritory.parent) {

                // Add the variant using the primary parent name.
                String nameVariant = mapcodeTerritory.parent.toString().concat("-").concat(childTerritoryName);
                if (false == nameVariant.contentEquals(name)) {
                    addNameWithSeperatorVariants(nameVariant, mapcodeTerritory);
                }

                // Add each variant using the parent alias names.
                for (String alias : mapcodeTerritory.parent.aliases) {
                    nameVariant = alias.concat("-").concat(childTerritoryName);
                    if (false == nameVariant.contentEquals(name)) {
                        addNameWithSeperatorVariants(nameVariant, mapcodeTerritory);
                    }
                }
            }
        }
    }

    private static void addNameWithSeperatorVariants(String name, MapcodeTerritory mapcodeTerritory) {
        addName(name, mapcodeTerritory);
        // Tolerate a space character in place of a hyphen.
        // (e.g. "US CA" in addition to "US-CA")
        if (name.contains("-")) {
            addName(name.replace('-', ' '), mapcodeTerritory);
        }
    }

    private static void addName(String name, MapcodeTerritory mapcodeTerritory) {
        if (nameMap.containsKey(name)) {
            ArrayList<MapcodeTerritory> arrayList = nameMap.get(name);
            // Add child territories in the order the parents are declared.
            // This results in consistent decoding of ambiguous territory names.
            MapcodeTerritory newTerritoryParent = mapcodeTerritory.getParent();
            if (newTerritoryParent == null && name.contentEquals(mapcodeTerritory.toString())) {
                // A primary identifier always takes priority.
                arrayList.clear();
                arrayList.add(mapcodeTerritory);
                return;
            } else if (newTerritoryParent != null) {
                for (int i = 0; i < arrayList.size(); i++) {
                    MapcodeTerritory existingTerritoryParent = arrayList.get(i).getParent();
                    if (existingTerritoryParent == null && arrayList.get(i).toString().contentEquals(name)) {
                        // A primary identifier always takes priority.
                        return;
                    }
                    if (existingTerritoryParent == null
                            || existingTerritoryParent.ordinal() > newTerritoryParent.ordinal()) {
                        arrayList.add(i, mapcodeTerritory);
                        return;
                    }
                }
            }
            arrayList.add(mapcodeTerritory);
        } else {
            ArrayList<MapcodeTerritory> arrayList = new ArrayList<MapcodeTerritory>();
            arrayList.add(mapcodeTerritory);
            nameMap.put(name, arrayList);
        }
    }

    public static MapcodeTerritory fromTerritoryCode(int territoryCode) {
        if (territoryCode < codeList.size()) {
            return codeList.get(territoryCode);
        }
        return null;
    }

    public static MapcodeTerritory fromString(String name) {
        return fromString(name, null);
    }

    public static MapcodeTerritory fromString(String name, MapcodeParentTerritory disambiguateOption) {
        ArrayList<MapcodeTerritory> mapcodeTerritories = nameMap.get(name);
        if (mapcodeTerritories != null) {
            if (disambiguateOption != null) {
                MapcodeTerritory parentTerritory = disambiguateOption.toMapCodeTerritory();
                for (MapcodeTerritory mapcodeTerritory : mapcodeTerritories) {
                    if (mapcodeTerritory.getParent() == parentTerritory) {
                        return mapcodeTerritory;
                    }
                }
            }
            return mapcodeTerritories.get(0);
        } else {
            // Check for a case such as USA-NLD (=NLD)
            int dividerLocation = Math.max(name.indexOf("-"), name.indexOf(" "));
            if (-1 != dividerLocation) {
                name = name.substring(dividerLocation + 1);
                return fromString(name, disambiguateOption);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        // Territory names contain "-". Enum constants must have this
        // substituted to "_"
        String name = this.name();
        return name.replace('_', '-');
    }

    public String toString(CodeFormat format) {
        if (format != CodeFormat.INTERNATIONAL) {
            int index = name().indexOf('_');
            if (index != -1) {
                String shortName = name().substring(index + 1);
                if (format == CodeFormat.MINIMAL || nameMap.get(shortName).size() == 1) {
                    return shortName;
                }
            }
        }
        return toString();
    }
    
    public boolean isState() {
        if (null != parent) {
            return true;
        }
        return false;
    }
    
    public boolean hasStates() {
        if (parentList.contains(this)) {
            return true;
        }
        return false;
    }
}
