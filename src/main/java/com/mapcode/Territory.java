/*
 * Copyright (C) 2014-2015 Stichting Mapcode Foundation (http://www.mapcode.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapcode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.mapcode.CheckArgs.checkNonnull;

/**
 * ----------------------------------------------------------------------------------------------
 * Mapcode public interface.
 * ----------------------------------------------------------------------------------------------
 * <p/>
 * This class defines the available territory codes as used by mapcode.
 */
public enum Territory {
    USA(410, "USA", null, new String[]{"US"}, new String[]{"United States of America", "America"}),
    IND(407, "India", null, new String[]{"IN"}),
    CAN(495, "Canada", null, new String[]{"CA"}),
    AUS(408, "Australia", null, new String[]{"AU"}),
    MEX(411, "Mexico", null, new String[]{"MX"}),
    BRA(409, "Brazil", null, new String[]{"BR"}),
    RUS(496, "Russia", null, new String[]{"RU"}),
    CHN(528, "China", null, new String[]{"CN"}),
    ATA(531, "Antarctica"),

    VAT(0, "Vatican City", null, null, new String[]{"Holy See)"}),
    MCO(1, "Monaco"),
    GIB(2, "Gibraltar"),
    TKL(3, "Tokelau"),
    CCK(4, "Cocos Islands", null, new String[]{"AU-CC", "AUS-CC"}, new String[]{"Keeling Islands"}),
    BLM(5, "Saint-Barthelemy"),
    NRU(6, "Nauru"),
    TUV(7, "Tuvalu"),
    MAC(8, "Macau", null, new String[]{"CN-92", "CHN-92", "CN-MC", "CHN-MC"}, new String[]{"Aomen"}),
    SXM(9, "Sint Maarten"),
    MAF(10, "Saint-Martin"),
    NFK(11, "Norfolk and Philip Island", null, new String[]{"AU-NF", "AUS-NF"},
            new String[]{"Philip Island"}),
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
    BES(33, "Bonaire, St Eustasuis and Saba", null, null, new String[]{"Saba", "St Eustasius"}),
    MDV(34, "Maldives"),
    SHN(35, "Saint Helena, Ascension and Tristan da Cunha", null, new String[]{"TAA", "ASC"},
            new String[]{"Ascension", "Tristan da Cunha"}),
    MLT(36, "Malta"),
    GRD(37, "Grenada"),
    VIR(38, "US Virgin Islands", null, new String[]{"US-VI", "USA-VI"}, new String[]{"Virgin Islands, US"}),
    MYT(39, "Mayotte"),
    SJM(40, "Svalbard and Jan Mayen", null, null, new String[]{"Jan Mayen", "Spitsbergen"}),
    VCT(41, "Saint Vincent and the Grenadines", null, null, new String[]{"Grenadines"}),
    HMD(42, "Heard Island and McDonald Islands", null, new String[]{"AU-HM", "AUS-HM"},
            new String[]{"McDonald Islands"}),
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
    HKG(61, "Hong Kong", null, new String[]{"CN-91", "CHN-91", "CN-HK", "CHN-HK"}, new String[]{"Xianggang"}),
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
    GBR(166, "United Kingdom", null, null,
            new String[]{"Scotland", "Great Britain", "Northern Ireland", "Ireland, Northern"}),
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
    TZA(216, "Tanzania"),
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
    MX_DIF(233, "Federal District", MEX, new String[]{"MX-DF"}),
    MX_TLA(234, "Tlaxcala", MEX, new String[]{"MX-TL"}),
    MX_MOR(235, "Morelos", MEX, new String[]{"MX-MO"}),
    MX_AGU(236, "Aguascalientes", MEX, new String[]{"MX-AG"}),
    MX_CL(237, "Colima", MEX, new String[]{"MX-COL"}),
    MX_QUE(238, "Queretaro", MEX, new String[]{"MX-QE"}),
    MX_HID(239, "Hidalgo", MEX, new String[]{"MX-HG"}),
    MX_MX(240, "Mexico State", MEX, new String[]{"MX-ME", "MX-MEX"}),
    MX_TAB(241, "Tabasco", MEX, new String[]{"MX-TB"}),
    MX_NAY(242, "Nayarit", MEX, new String[]{"MX-NA"}),
    MX_GUA(243, "Guanajuato", MEX, new String[]{"MX-GT"}),
    MX_PUE(244, "Puebla", MEX, new String[]{"MX-PB"}),
    MX_YUC(245, "Yucatan", MEX, new String[]{"MX-YU"}),
    MX_ROO(246, "Quintana Roo", MEX, new String[]{"MX-QR"}),
    MX_SIN(247, "Sinaloa", MEX, new String[]{"MX-SI"}),
    MX_CAM(248, "Campeche", MEX, new String[]{"MX-CM"}),
    MX_MIC(249, "Michoacan", MEX, new String[]{"MX-MI"}),
    MX_SLP(250, "San Luis Potosi", MEX, new String[]{"MX-SL"}),
    MX_GRO(251, "Guerrero", MEX, new String[]{"MX-GR"}),
    MX_NLE(252, "Nuevo Leon", MEX, new String[]{"MX-NL"}),
    MX_BCN(253, "Baja California", MEX, new String[]{"MX-BC"}),
    MX_VER(254, "Veracruz", MEX, new String[]{"MX-VE"}),
    MX_CHP(255, "Chiapas", MEX, new String[]{"MX-CS"}),
    MX_BCS(256, "Baja California Sur", MEX, new String[]{"MX-BS"}),
    MX_ZAC(257, "Zacatecas", MEX, new String[]{"MX-ZA"}),
    MX_JAL(258, "Jalisco", MEX, new String[]{"MX-JA"}),
    MX_TAM(259, "Tamaulipas", MEX, new String[]{"MX-TM"}),
    MX_OAX(260, "Oaxaca", MEX, new String[]{"MX-OA"}),
    MX_DUR(261, "Durango", MEX, new String[]{"MX-DG"}),
    MX_COA(262, "Coahuila", MEX, new String[]{"MX-CO"}),
    MX_SON(263, "Sonora", MEX, new String[]{"MX-SO"}),
    MX_CHH(264, "Chihuahua", MEX, new String[]{"MX-CH"}),
    GRL(265, "Greenland"),
    SAU(266, "Saudi Arabia"),
    COD(267, "Congo-Kinshasa"),
    DZA(268, "Algeria"),
    KAZ(269, "Kazakhstan"),
    ARG(270, "Argentina"),
    IN_DD(271, "Daman and Diu", IND),
    IN_DN(272, "Dadra and Nagar Haveli", IND),
    IN_CH(273, "Chandigarh", IND),
    IN_AN(274, "Andaman and Nicobar", IND),
    IN_LD(275, "Lakshadweep", IND),
    IN_DL(276, "Delhi", IND),
    IN_ML(277, "Meghalaya", IND),
    IN_NL(278, "Nagaland", IND),
    IN_MN(279, "Manipur", IND),
    IN_TR(280, "Tripura", IND),
    IN_MZ(281, "Mizoram", IND),
    IN_SK(282, "Sikkim", IND),
    IN_PB(283, "Punjab", IND),
    IN_HR(284, "Haryana", IND),
    IN_AR(285, "Arunachal Pradesh", IND),
    IN_AS(286, "Assam", IND),
    IN_BR(287, "Bihar", IND),
    IN_UT(288, "Uttarakhand", IND, new String[]{"IN-UK"}),
    IN_GA(289, "Goa", IND),
    IN_KL(290, "Kerala", IND),
    IN_TN(291, "Tamil Nadu", IND),
    IN_HP(292, "Himachal Pradesh", IND),
    IN_JK(293, "Jammu and Kashmir", IND),
    IN_CT(294, "Chhattisgarh", IND, new String[]{"IN-CG"}),
    IN_JH(295, "Jharkhand", IND),
    IN_KA(296, "Karnataka", IND),
    IN_RJ(297, "Rajasthan", IND),
    IN_OR(298, "Odisha", IND, new String[]{"IN-OD"}, new String[]{"Orissa"}),
    IN_GJ(299, "Gujarat", IND),
    IN_WB(300, "West Bengal", IND),
    IN_MP(301, "Madhya Pradesh", IND),
    IN_TG(302, "Telangana", IND),
    IN_AP(303, "Andhra Pradesh", IND),
    IN_MH(304, "Maharashtra", IND),
    IN_UP(305, "Uttar Pradesh", IND),
    IN_PY(306, "Puducherry", IND),
    AU_NSW(307, "New South Wales", AUS),
    AU_ACT(308, "Australian Capital Territory", AUS),
    AU_JBT(309, "Jervis Bay Territory", AUS),
    AU_NT(310, "Northern Territory", AUS),
    AU_SA(311, "South Australia", AUS),
    AU_TAS(312, "Tasmania", AUS),
    AU_VIC(313, "Victoria", AUS),
    AU_WA(314, "Western Australia", AUS),
    AU_QLD(315, "Queensland", AUS),
    BR_DF(316, "Distrito Federal", BRA),
    BR_SE(317, "Sergipe", BRA),
    BR_AL(318, "Alagoas", BRA),
    BR_RJ(319, "Rio de Janeiro", BRA),
    BR_ES(320, "Espirito Santo", BRA),
    BR_RN(321, "Rio Grande do Norte", BRA),
    BR_PB(322, "Paraiba", BRA),
    BR_SC(323, "Santa Catarina", BRA),
    BR_PE(324, "Pernambuco", BRA),
    BR_AP(325, "Amapa", BRA),
    BR_CE(326, "Ceara", BRA),
    BR_AC(327, "Acre", BRA),
    BR_PR(328, "Parana", BRA),
    BR_RR(329, "Roraima", BRA),
    BR_RO(330, "Rondonia", BRA),
    BR_SP(331, "Sao Paulo", BRA),
    BR_PI(332, "Piaui", BRA),
    BR_TO(333, "Tocantins", BRA),
    BR_RS(334, "Rio Grande do Sul", BRA),
    BR_MA(335, "Maranhao", BRA),
    BR_GO(336, "Goias", BRA),
    BR_MS(337, "Mato Grosso do Sul", BRA),
    BR_BA(338, "Bahia", BRA),
    BR_MG(339, "Minas Gerais", BRA),
    BR_MT(340, "Mato Grosso", BRA),
    BR_PA(341, "Para", BRA),
    BR_AM(342, "Amazonas", BRA),
    US_DC(343, "District of Columbia", USA),
    US_RI(344, "Rhode Island", USA),
    US_DE(345, "Delaware", USA),
    US_CT(346, "Connecticut", USA),
    US_NJ(347, "New Jersey", USA),
    US_NH(348, "New Hampshire", USA),
    US_VT(349, "Vermont", USA),
    US_MA(350, "Massachusetts", USA),
    US_HI(351, "Hawaii", USA, new String[]{"US-MID"}),
    US_MD(352, "Maryland", USA),
    US_WV(353, "West Virginia", USA),
    US_SC(354, "South Carolina", USA),
    US_ME(355, "Maine", USA),
    US_IN(356, "Indiana", USA),
    US_KY(357, "Kentucky", USA),
    US_TN(358, "Tennessee", USA),
    US_VA(359, "Virginia", USA),
    US_OH(360, "Ohio", USA),
    US_PA(361, "Pennsylvania", USA),
    US_MS(362, "Mississippi", USA),
    US_LA(363, "Louisiana", USA),
    US_AL(364, "Alabama", USA),
    US_AR(365, "Arkansas", USA),
    US_NC(366, "North Carolina", USA),
    US_NY(367, "New York", USA),
    US_IA(368, "Iowa", USA),
    US_IL(369, "Illinois", USA),
    US_GA(370, "Georgia", USA),
    US_WI(371, "Wisconsin", USA),
    US_FL(372, "Florida", USA),
    US_MO(373, "Missouri", USA),
    US_OK(374, "Oklahoma", USA),
    US_ND(375, "North Dakota", USA),
    US_WA(376, "Washington", USA),
    US_SD(377, "South Dakota", USA),
    US_NE(378, "Nebraska", USA),
    US_KS(379, "Kansas", USA),
    US_ID(380, "Idaho", USA),
    US_UT(381, "Utah", USA),
    US_MN(382, "Minnesota", USA),
    US_MI(383, "Michigan", USA),
    US_WY(384, "Wyoming", USA),
    US_OR(385, "Oregon", USA),
    US_CO(386, "Colorado", USA),
    US_NV(387, "Nevada", USA),
    US_AZ(388, "Arizona", USA),
    US_NM(389, "New Mexico", USA),
    US_MT(390, "Montana", USA),
    US_CA(391, "California", USA),
    US_TX(392, "Texas", USA),
    US_AK(393, "Alaska", USA),
    CA_BC(394, "British Columbia", CAN),
    CA_AB(395, "Alberta", CAN),
    CA_ON(396, "Ontario", CAN),
    CA_QC(397, "Quebec", CAN),
    CA_SK(398, "Saskatchewan", CAN),
    CA_MB(399, "Manitoba", CAN),
    CA_NL(400, "Newfoundland", CAN),
    CA_NB(401, "New Brunswick", CAN),
    CA_NS(402, "Nova Scotia", CAN),
    CA_PE(403, "Prince Edward Island", CAN),
    CA_YT(404, "Yukon", CAN),
    CA_NT(405, "Northwest Territories", CAN),
    CA_NU(406, "Nunavut", CAN),
    RU_MOW(412, "Moscow", RUS),
    RU_SPE(413, "Saint Petersburg", RUS),
    RU_KGD(414, "Kaliningrad Oblast", RUS),
    RU_IN(415, "Ingushetia Republic", RUS),
    RU_AD(416, "Adygea Republic", RUS),
    RU_SE(417, "North Ossetia-Alania Republic", RUS),
    RU_KB(418, "Kabardino-Balkar Republic", RUS),
    RU_KC(419, "Karachay-Cherkess Republic", RUS),
    RU_CE(420, "Chechen Republic", RUS),
    RU_CU(421, "Chuvash Republic", RUS),
    RU_IVA(422, "Ivanovo Oblast", RUS),
    RU_LIP(423, "Lipetsk Oblast", RUS),
    RU_ORL(424, "Oryol Oblast", RUS),
    RU_TUL(425, "Tula Oblast", RUS),
    RU_BE(426, "Belgorod Oblast", RUS, new String[]{"RU-BEL"}),
    RU_VLA(427, "Vladimir Oblast", RUS),
    RU_KRS(428, "Kursk Oblast", RUS),
    RU_KLU(429, "Kaluga Oblast", RUS),
    RU_TT(430, "Tambov Oblast", RUS, new String[]{"RU-TAM"}),
    RU_BRY(431, "Bryansk Oblast", RUS),
    RU_YAR(432, "Yaroslavl Oblast", RUS),
    RU_RYA(433, "Ryazan Oblast", RUS),
    RU_AST(434, "Astrakhan Oblast", RUS),
    RU_MOS(435, "Moscow Oblast", RUS),
    RU_SMO(436, "Smolensk Oblast", RUS),
    RU_DA(437, "Dagestan Republic", RUS),
    RU_VOR(438, "Voronezh Oblast", RUS),
    RU_NGR(439, "Novgorod Oblast", RUS),
    RU_PSK(440, "Pskov Oblast", RUS),
    RU_KOS(441, "Kostroma Oblast", RUS),
    RU_STA(442, "Stavropol Krai", RUS),
    RU_KDA(443, "Krasnodar Krai", RUS),
    RU_KL(444, "Kalmykia Republic", RUS),
    RU_TVE(445, "Tver Oblast", RUS),
    RU_LEN(446, "Leningrad Oblast", RUS),
    RU_ROS(447, "Rostov Oblast", RUS),
    RU_VGG(448, "Volgograd Oblast", RUS),
    RU_VLG(449, "Vologda Oblast", RUS),
    RU_MUR(450, "Murmansk Oblast", RUS),
    RU_KR(451, "Karelia Republic", RUS),
    RU_NEN(452, "Nenets Autonomous Okrug", RUS),
    RU_KO(453, "Komi Republic", RUS),
    RU_ARK(454, "Arkhangelsk Oblast", RUS),
    RU_MO(455, "Mordovia Republic", RUS),
    RU_NIZ(456, "Nizhny Novgorod Oblast", RUS),
    RU_PNZ(457, "Penza Oblast", RUS),
    RU_KI(458, "Kirov Oblast", RUS, new String[]{"RU-KIR"}),
    RU_ME(459, "Mari El Republic", RUS),
    RU_ORE(460, "Orenburg Oblast", RUS),
    RU_ULY(461, "Ulyanovsk Oblast", RUS),
    RU_PM(462, "Perm Krai", RUS, new String[]{"RU-PER"}),
    RU_BA(463, "Bashkortostan Republic", RUS),
    RU_UD(464, "Udmurt Republic", RUS),
    RU_TA(465, "Tatarstan Republic", RUS),
    RU_SAM(466, "Samara Oblast", RUS),
    RU_SAR(467, "Saratov Oblast", RUS),
    RU_YAN(468, "Yamalo-Nenets", RUS),
    RU_KM(469, "Khanty-Mansi", RUS, new String[]{"RU-KHM"}),
    RU_SVE(470, "Sverdlovsk Oblast", RUS),
    RU_TYU(471, "Tyumen Oblast", RUS),
    RU_KGN(472, "Kurgan Oblast", RUS),
    RU_CH(473, "Chelyabinsk Oblast", RUS, new String[]{"RU-CHE"}),
    RU_BU(474, "Buryatia Republic", RUS),
    RU_ZAB(475, "Zabaykalsky Krai", RUS),
    RU_IRK(476, "Irkutsk Oblast", RUS),
    RU_NVS(477, "Novosibirsk Oblast", RUS),
    RU_TOM(478, "Tomsk Oblast", RUS),
    RU_OMS(479, "Omsk Oblast", RUS),
    RU_KK(480, "Khakassia Republic", RUS),
    RU_KEM(481, "Kemerovo Oblast", RUS),
    RU_AL(482, "Altai Republic", RUS),
    RU_ALT(483, "Altai Krai", RUS),
    RU_TY(484, "Tuva Republic", RUS),
    RU_KYA(485, "Krasnoyarsk Krai", RUS),
    RU_MAG(486, "Magadan Oblast", RUS),
    RU_CHU(487, "Chukotka Okrug", RUS),
    RU_KAM(488, "Kamchatka Krai", RUS),
    RU_SAK(489, "Sakhalin Oblast", RUS),
    RU_PO(490, "Primorsky Krai", RUS, new String[]{"RU-PRI"}),
    RU_YEV(491, "Jewish Autonomous Oblast", RUS),
    RU_KHA(492, "Khabarovsk Krai", RUS),
    RU_AMU(493, "Amur Oblast", RUS),
    RU_SA(494, "Sakha Republic", RUS, null, new String[]{"Yakutia Republic"}),
    CN_SH(497, "Shanghai", CHN, new String[]{"CN-31"}),
    CN_TJ(498, "Tianjin", CHN, new String[]{"CN-12"}),
    CN_BJ(499, "Beijing", CHN, new String[]{"CN-11"}),
    CN_HI(500, "Hainan", CHN, new String[]{"CN-46"}),
    CN_NX(501, "Ningxia Hui", CHN, new String[]{"CN-64"}),
    CN_CQ(502, "Chongqing", CHN, new String[]{"CN-50"}),
    CN_ZJ(503, "Zhejiang", CHN, new String[]{"CN-33"}),
    CN_JS(504, "Jiangsu", CHN, new String[]{"CN-32"}),
    CN_FJ(505, "Fujian", CHN, new String[]{"CN-35"}),
    CN_AH(506, "Anhui", CHN, new String[]{"CN-34"}),
    CN_LN(507, "Liaoning", CHN, new String[]{"CN-21"}),
    CN_SD(508, "Shandong", CHN, new String[]{"CN-37"}),
    CN_SX(509, "Shanxi", CHN, new String[]{"CN-14"}),
    CN_JX(510, "Jiangxi", CHN, new String[]{"CN-36"}),
    CN_HA(511, "Henan", CHN, new String[]{"CN-41"}),
    CN_GZ(512, "Guizhou", CHN, new String[]{"CN-52"}),
    CN_GD(513, "Guangdong", CHN, new String[]{"CN-44"}),
    CN_HB(514, "Hubei", CHN, new String[]{"CN-42"}),
    CN_JL(515, "Jilin", CHN, new String[]{"CN-22"}),
    CN_HE(516, "Hebei", CHN, new String[]{"CN-13"}),
    CN_SN(517, "Shaanxi", CHN, new String[]{"CN-61"}),
    CN_NM(518, "Nei Mongol", CHN, new String[]{"CN-15"}, new String[]{"Inner Mongolia"}),
    CN_HL(519, "Heilongjiang", CHN, new String[]{"CN-23"}),
    CN_HN(520, "Hunan", CHN, new String[]{"CN-43"}),
    CN_GX(521, "Guangxi Zhuang", CHN, new String[]{"CN-45"}),
    CN_SC(522, "Sichuan", CHN, new String[]{"CN-51"}),
    CN_YN(523, "Yunnan", CHN, new String[]{"CN-53"}),
    CN_XZ(524, "Xizang", CHN, new String[]{"CN-54"}, new String[]{"Tibet"}),
    CN_GS(525, "Gansu", CHN, new String[]{"CN-62"}),
    CN_QH(526, "Qinghai", CHN, new String[]{"CN-63"}),
    CN_XJ(527, "Xinjiang Uyghur", CHN, new String[]{"CN-65"}),
    UMI(529, "United States Minor Outlying Islands", null, new String[]{"US-UM", "USA-UM", "JTN"}),
    CPT(530, "Clipperton Island"),
    AAA(532, "International", null, null, new String[]{"Worldwide", "Earth"});

    @SuppressWarnings("PublicStaticCollectionField")
    @Nonnull
    public static final Set<Territory> PARENT_TERRITORIES = Collections.unmodifiableSet(
            EnumSet.of(USA, IND, CAN, AUS, MEX, BRA, RUS, CHN, ATA));

    private final int number;
    @Nonnull
    private final String fullName;
    @Nullable
    private final Territory parentTerritory;
    @Nonnull
    private final String[] aliases;
    @Nonnull
    private final String[] fullNameAliases;

    /**
     * Return the numeric territory code for a territory. Package private, because territory numbers are no longer
     * exposed publicly.
     *
     * @return Integer territory code.
     */
    int getNumber() {
        return number;
    }

    /**
     * Return the full name of the territory.
     *
     * @return Full name.
     */
    @Nonnull
    public String getFullName() {
        return fullName;
    }

    /**
     * Return aliases (if any) for a territory. If there are no aliases, the array is empty.
     *
     * @return Aliases. Empty if no aliases exist.
     */
    @Nonnull
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Return the parent territory.
     *
     * @return Parent territory for this territory. Null if no parent territory exists.
     */
    @Nullable
    public Territory getParentTerritory() {
        return parentTerritory;
    }

    /**
     * Return aliases (if any) for the full name of a territory. If there are no aliases, the array is empty.
     *
     * @return Aliases. Empty if no aliases exist.
     */
    @Nonnull
    public String[] getFullNameAliases() {
        return fullNameAliases;
    }

    /**
     * Return the territory for a specific code. Package private, because territory numbers are no longer exposed
     * publicly.
     *
     * @param number Numeric territory code.
     * @return Territory.
     * @throws UnknownTerritoryException Thrown if incorrect numeric or alphanumeric code.
     */
    @Nonnull
    static Territory fromNumber(final int number) throws UnknownTerritoryException {
        if ((number < 0) || (number >= codeList.size())) {
            throw new UnknownTerritoryException(number);
        }
        return codeList.get(number);
    }

    /**
     * Get a territory from a mapcode territory abbreviation (or a territory name). Note that the provided abbreviation is NOT an
     * ISO code: it's a mapcode prefix. As local mapcodes for subdivisions have been optimized to prefer to use 2-character
     * subdivisions codes in local codes, subdivisions are preferred over countries in this case.
     * <p/>
     * For example, fromString("AS") returns {@link Territory#IN_AS} rather than {@link Territory#ASM} and
     * fromString("BR") returns {@link Territory#IN_BR} rather than {@link Territory#BRA}.
     * <p/>
     * This behavior is intentional as local mapcodes are designed to be as short as possible. A mapcode within
     * the Indian state Bihar should therefore be able to specified as "BR 49.46M3" rather "IN-BR 49.46M3".
     * <p/>
     * Brazilian mapcodes, on the other hand, would be specified as "BRA BDHP.JK39-1D", using the ISO 3 letter code.
     *
     * @param alphaCode Territory name or alphanumeric code.
     * @return Territory.
     * @throws UnknownTerritoryException Thrown if incorrect numeric or alphanumeric code.
     */
    @Nonnull
    public static Territory fromString(@Nonnull final String alphaCode) throws UnknownTerritoryException {
        checkNonnull("alphaCode", alphaCode);
        return createFromString(alphaCode, null);
    }

    /**
     * Get a territory from a name, specifying a parent territory for disambiguation.
     *
     * @param alphaCode       Territory, alphanumeric code. See {@link #fromString(String)}
     *                        for an explanation of the format for this name. (This is NOT strictly an ISO code!)
     * @param parentTerritory Parent territory.
     * @return Territory.
     * @throws UnknownTerritoryException Thrown if the territory is not found given the parentTerritory.
     */
    @Nonnull
    public static Territory fromString(
            @Nonnull final String alphaCode,
            @Nonnull final Territory parentTerritory) throws UnknownTerritoryException {
        checkNonnull("alphaCode", alphaCode);
        checkNonnull("parentTerritory", parentTerritory);
        if (!PARENT_TERRITORIES.contains(parentTerritory)) {
            throw new IllegalArgumentException("Parameter " + parentTerritory + " is not a parent territory");
        }
        return createFromString(alphaCode, parentTerritory);
    }

    /**
     * Return the international value of the territory name, with dashes rather than underscores.
     * This is the same as {@link #toAlphaCode(AlphaCodeFormat)} for {@link AlphaCodeFormat#INTERNATIONAL}.
     *
     * @param alphabet Alphabet. May be null.
     * @return Territory name. Underscores have been replaced with dashes.
     */
    @Nonnull
    public String toString(@Nullable final Alphabet alphabet) {
        return toAlphaCode(AlphaCodeFormat.INTERNATIONAL, alphabet);
    }

    /**
     * Return the international value of the territory name, with dashes rather than underscores.
     * This is the same as {@link #toAlphaCode(AlphaCodeFormat)} for {@link AlphaCodeFormat#INTERNATIONAL}.
     *
     * @return Territory name. Underscores have been replaced with dashes.
     */
    @Override
    @Nonnull
    public String toString() {
        return toString(null);
    }

    /**
     * Enumeration that specifies the format for mapcodes.
     */
    public enum AlphaCodeFormat {
        INTERNATIONAL,          // Same as name() with underscores replaces with dashes.
        MINIMAL_UNAMBIGUOUS,    // Minimal code, which is still unambiguous.
        MINIMAL                 // Minimal code, may be ambiguous, eg. RJ instead of IN-RJ.
    }

    /**
     * Return the territory name, given a specific territory name format.
     *
     * @param format   Format to be used.
     * @param alphabet Alphabet. May be null.
     * @return Mapcode.
     */
    @Nonnull
    public String toAlphaCode(@Nonnull final AlphaCodeFormat format, @Nullable final Alphabet alphabet) {
        checkNonnull("format", format);
        String result = name().replace('_', '-');
        if (format != AlphaCodeFormat.INTERNATIONAL) {
            final int index = name().indexOf('_');
            if (index != -1) {
                assert name().length() > (index + 1);
                final String shortName = name().substring(index + 1);
                if ((format == AlphaCodeFormat.MINIMAL) || (nameMap.get(shortName).size() == 1)) {
                    result = shortName;
                }
            }
        }
        if (alphabet != null) {
            try {
                result = Mapcode.convertStringToAlphabet(result, alphabet);
            } catch (final IllegalArgumentException ignored) {
                // Simply return result if translation to alphabet fails.
            }
        }
        return result;
    }

    @Nonnull
    public String toAlphaCode(@Nonnull final AlphaCodeFormat format) {
        return toAlphaCode(format, null);
    }

    /**
     * Returns if this territory is a subdivisions of another territory.
     *
     * @return True if this territory has a parent territory.
     */
    public boolean isSubdivision() {
        return parentTerritory != null;
    }

    /**
     * Returns if this territory contains other territory subdivisions.
     *
     * @return True if this territory contains other territory subdivisions.
     */
    public boolean hasSubdivisions() {
        return parentList.contains(this);
    }

    /**
     * Private constructors to create a territory code.
     */
    private Territory(
            final int number,
            @Nonnull final String fullName) {
        this(number, fullName, null, null, null);
    }

    private Territory(
            final int number,
            @Nonnull final String fullName,
            @Nullable final Territory parentTerritory) {
        this(number, fullName, parentTerritory, null, null);
    }

    private Territory(
            final int number,
            @Nonnull final String fullName,
            @Nullable final Territory parentTerritory,
            @Nullable final String[] aliases) {
        this(number, fullName, parentTerritory, aliases, null);
    }

    private Territory(
            final int number,
            @Nonnull final String fullName,
            @Nullable final Territory parentTerritory,
            @Nullable final String[] aliases,
            @Nullable final String[] fullNameAliases) {
        this.number = number;
        this.fullName = fullName;
        this.parentTerritory = parentTerritory;
        this.aliases = (aliases == null) ? new String[]{} : aliases;
        this.fullNameAliases = (fullNameAliases == null) ? new String[]{} : fullNameAliases;
    }

    @Nonnull
    private static final List<Territory> codeList;
    @Nonnull
    private static final Map<String, List<Territory>> nameMap;
    @Nonnull
    private static final List<Territory> parentList;

    /**
     * Static checking of the static data structures.
     */
    static {
        final String errorPrefix = "Initializing error: ";
        codeList = new ArrayList<Territory>();
        nameMap = new HashMap<String, List<Territory>>();
        parentList = new ArrayList<Territory>();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        final Set<Integer> territoryCodes = new HashSet<Integer>();
        final Set<String> namesSet = new HashSet<String>();

        for (final Territory territory : Territory.values()) {
            final int territoryNumber = territory.getNumber();
            if ((territoryNumber < 0) || (territoryNumber >= Territory.values().length)) {
                throw new ExceptionInInitializerError(errorPrefix + "territory number out of range: " + territoryNumber);

            }
            if (territoryCodes.contains(territoryNumber)) {
                throw new ExceptionInInitializerError(errorPrefix + "non-unique territory number: " + territoryNumber);
            }
            territoryCodes.add(territory.getNumber());

            final int initialCodeListSize = codeList.size();
            for (int i = initialCodeListSize; i <= territory.number; i++) {
                codeList.add(null);
            }
            codeList.set(territory.number, territory);
            if ((territory.parentTerritory != null) && !parentList.contains(territory.parentTerritory)) {
                parentList.add(territory.parentTerritory);
            }

            // Add territory codes and alias codes.
            if (namesSet.contains(territory.toString())) {
                throw new ExceptionInInitializerError(errorPrefix + "non-unique territory: " + territory.toString());
            }
            namesSet.add(territory.toString());
            addNameWithParentVariants(territory.toString(), territory);
            for (final String alias : territory.aliases) {
                if (namesSet.contains(alias)) {
                    throw new ExceptionInInitializerError(errorPrefix + "non-unique alias: " + alias);
                }
                namesSet.add(alias);
                addNameWithParentVariants(alias, territory);
            }

            // Add territory fullnames and aliases as well. Skip special case: territory name == territory code (e.g. USA).
            if (namesSet.contains(territory.fullName.toUpperCase()) && !territory.toString().equals(territory.fullName.toUpperCase())) {
                throw new ExceptionInInitializerError(errorPrefix + "non-unique fullName: " + territory.fullName.toUpperCase());
            }
            addNameWithParentVariants(territory.fullName.toUpperCase(), territory);
            for (final String fullNameAlias : territory.fullNameAliases) {
                if (namesSet.contains(fullNameAlias.toUpperCase())) {
                    throw new ExceptionInInitializerError(errorPrefix + "non-unique fullName alias: " + fullNameAlias);
                }
                namesSet.add(fullNameAlias.toUpperCase());
                addNameWithParentVariants(fullNameAlias.toUpperCase(), territory);
            }
            min = Math.min(min, territory.number);
            max = Math.max(max, territory.number);
        }
        assert territoryCodes.size() == Territory.values().length;

        // Check for missing codes.
        if (!((min == 0) && (max == (Territory.values().length - 1)))) {
            throw new ExceptionInInitializerError(errorPrefix + "incorrect min/max: " + min + '/' + max);
        }
    }

    /**
     * Get a territory from a name, specifying a parent territory for disambiguation.
     *
     * @param alphaCode       Territory, alphanumeric code.
     * @param parentTerritory Parent territory.
     * @return Territory.
     * @throws UnknownTerritoryException Thrown if the territory is not found.
     */
    @Nonnull
    private static Territory createFromString(
            @Nonnull final String alphaCode,
            @Nullable final Territory parentTerritory) throws UnknownTerritoryException {
        final String trimmed = Mapcode.convertStringToPlainAscii(alphaCode.trim().replace('_', '-')).toUpperCase();

        // Try as alpha code.
        final List<Territory> territories = nameMap.get(trimmed);
        if (territories != null) {
            if (parentTerritory == null) {
                return territories.get(0);
            }
            for (final Territory territory : territories) {
                if (territory.getParentTerritory() == parentTerritory) {
                    return territory;
                }
            }
            throw new UnknownTerritoryException(trimmed);
        }

        // Check for a case such as USA-NLD (=NLD)
        final int dividerLocation = Math.max(trimmed.indexOf('-'), trimmed.indexOf(' '));
        if (dividerLocation >= 0) {
            return createFromString(trimmed.substring(dividerLocation + 1), parentTerritory);
        }
        throw new UnknownTerritoryException(trimmed);
    }

    /**
     * Private helper method to add names for a territory.
     *
     * @param name      Name to add.
     * @param territory Territory.
     */
    private static void addNameWithParentVariants(
            @Nonnull final String name,
            @Nonnull final Territory territory) {

        // Add the name as provided
        addNameWithSeperatorVariants(name, territory);

        if (name.contains("-")) {
            final String childTerritoryName = name.substring(name.indexOf('-') + 1);

            // Tolerate a child territory specified without the parent.
            // (e.g. "CA" rather than "US-CA")
            addName(childTerritoryName, territory);

            if (territory.parentTerritory != null) {

                // Add the variant using the primary parent name.
                String nameVariant = (territory.parentTerritory.toString() + '-') + childTerritoryName;
                if (!nameVariant.contentEquals(name)) {
                    addNameWithSeperatorVariants(nameVariant, territory);
                }

                // Add each variant using the parent alias names.
                for (final String alias : territory.parentTerritory.aliases) {
                    nameVariant = alias + '-' + childTerritoryName;
                    if (!nameVariant.contentEquals(name)) {
                        addNameWithSeperatorVariants(nameVariant, territory);
                    }
                }
            }
        }
    }

    private static void addNameWithSeperatorVariants(@Nonnull final String name, @Nonnull final Territory territory) {
        addName(name, territory);

        // Tolerate a space character in place of a hyphen.
        // (e.g. "US CA" in addition to "US-CA")
        if (name.contains("-")) {
            addName(name.replace('-', ' '), territory);
        }
    }

    private static void addName(@Nonnull final String name, @Nonnull final Territory territory) {
        if (nameMap.containsKey(name)) {
            final List<Territory> territories = nameMap.get(name);

            // Add child territories in the order the parents are declared.
            // This results in consistent decoding of ambiguous territory names.
            final Territory newTerritoryParent = territory.getParentTerritory();
            if ((newTerritoryParent == null) && name.equals(territory.toString())) {

                // A primary identifier always takes priority.
                territories.clear();
                territories.add(territory);
                return;
            }

            if (newTerritoryParent != null) {
                for (int i = 0; i < territories.size(); i++) {

                    final Territory existingTerritoryParent = territories.get(i).getParentTerritory();
                    if ((existingTerritoryParent == null) && territories.get(i).toString().contentEquals(name)) {
                        // A primary identifier always takes priority.
                        return;
                    }
                    if ((existingTerritoryParent == null) ||
                            (existingTerritoryParent.ordinal() > newTerritoryParent.ordinal())) {
                        territories.add(i, territory);
                        return;
                    }
                }
            }
            territories.add(territory);
        } else {
            final ArrayList<Territory> arrayList = new ArrayList<Territory>();
            arrayList.add(territory);
            nameMap.put(name, arrayList);
        }
    }
}
