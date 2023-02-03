package ti4.map_ttpg;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.Chromaticity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import ti4.commands.map.AddTileList;
import ti4.commands.player.PlanetAdd;
import ti4.commands.status.ScorePublic;
import ti4.commands.tokens.AddControl;
import ti4.commands.tokens.AddToken;
import ti4.generator.Mapper;
import ti4.generator.PositionMapper;
import ti4.helpers.AliasHandler;
import ti4.helpers.Constants;
import ti4.helpers.Storage;
import ti4.map.Map;
import ti4.map.MapManager;
import ti4.map.MapSaveLoadManager;
import ti4.map.Player;
import ti4.map.Tile;
import ti4.map.UnitHolder;
import ti4.map_ttpg.TTPGPlayer;

public class ConvertTTPGtoAsync {

    private static final ArrayList<String> validColours = new ArrayList<String>(){{
        add("W"); //White
        add("B"); //Blue
        add("P"); //Purple
        add("Y"); //Yellow
        add("R"); //Red
        add("G"); //Green
        add("E"); //Orange
        add("K"); //Pink
    }};

    private static final ArrayList<String> validUnits = new ArrayList<String>(){{
        add("c"); //carrier
        add("d"); //dreadnought
        add("f"); //fighter
        add("h"); //flagship
        add("i"); //infantry
        add("m"); //mech
        add("o"); //control_token
        add("p"); //pds
        add("r"); //cruiser
        add("s"); //space_dock
        add("t"); //command_token
        add("w"); //war_sun
        add("y"); //destroyer
    }};

    private static final ArrayList<String> validAttachments = new ArrayList<String>(){{
        add("C"); //cybernetic_research_facility_face
        add("I"); //biotic_research_facility_face
        add("O"); //propulsion_research_facility_face
        add("W"); //warfare_research_facility_face
        add("a"); //alpha_wormhole
        add("b"); //beta_wormhole
        add("c"); //cybernetic_Research_Facility_back
        add("d"); //dyson_sphere
        add("e"); //frontier
        add("f"); //nano_forge
        add("g"); //gamma_wormhole
        add("h"); //grav_tear
        add("i"); //biotic_research_facility_back
        add("j"); //tomb_of_emphidia
        add("k"); //mirage
        add("l"); //stellar_converter
        add("m"); //mining_world
        add("n"); //ion_storm
        add("o"); //propulsion_research_facility_back
        add("p"); //paradise_world
        add("q"); //ul_sleeper
        add("r"); //rich_world
        add("t"); //ul_terraform
        add("u"); //ul_geoform
        add("w"); //warfare_research_facility_back
        add("x"); //lazax_survivors
        add("z"); //dmz
    }};

    public static final java.util.Map<String,String> fakePlayers = new HashMap<String, String>() {
        {
            put("481860200169472030", "PrisonerOne");
            put("345897843757678603", "TerTerro");
            put("150809002974904321", "Holytispoon");
            put("936295970671566879", "somno");
            put("426282231234035722", "Son of Leto(UTC-6)");
            put("960683086570487848", "TheEpicNerd");
            // put("947763140517560331", "TI4 Game Management");
            // put("1059869343636263023", "TI4-Bot-Test");
            // put("814883082033037383", "Map Bot");
            // put("235148962103951360", "Carl-bot");
            // put("936929561302675456", "Midjourney Bot");
            // put("812171459564011580", "RoboDane");
            // put("572698679618568193", "Dicecord");
        }
    };
    


    public static void main(String[] args) throws Exception {
        PositionMapper.init();
        Mapper.init();
        AliasHandler.init();
        Storage.init();
        // String jsonSource = readFileAsString("storage/ttpg_exports/TTPG-Export.json");
        // JsonNode node = parse(jsonSource);
        TTPGMap ttpgMap = getTTPGMapFromJsonFile("storage/ttpg_exports/TTPG-Export-Hadouken.json");

        Map map = ConvertTTPGMaptoAsyncMap(ttpgMap);

        // JsonNode node = toJson(map);
        // System.out.println(generateString(node,true));

        MapSaveLoadManager.saveMap(map);
        // Map newMap = MapSaveLoadManager.loadMap
    }

    public static Map ConvertTTPGMaptoAsyncMap(TTPGMap ttpgMap){
        Mapper.init();
        Map asyncMap = new Map() {
            {
                setOwnerID("481860200169472030");
                setOwnerName("PrisonerOne");
                setPlayerCountForMap(ttpgMap.getPlayers().size());
                setVp(ttpgMap.getScoreboard());
                setRound(ttpgMap.getRound());
                setName("ttpgimport");// + currentDateTime());
            }
        };
        
        for (String objective : ttpgMap.getObjectives().getPublicObjectivesI()) {
            asyncMap.addSpecificStage1(AliasHandler.resolvePublicObjective(objective));
        }
        for (String objective : ttpgMap.getObjectives().getPublicObjectivesII()) {
            asyncMap.addSpecificStage2(AliasHandler.resolvePublicObjective(objective));
        }


        // System.out.println(asyncMap.getRevealedPublicObjectives());

        // System.out.println("Mapped? " + AddTileList.setMapTileList(null, ttpgMap.getMapString(), asyncMap));

        Integer index = 0;
        LinkedHashMap<String, Player> asyncPlayers = asyncMap.getPlayers();

        //PLAYER
        for (Entry<String,String> fakePlayer : fakePlayers.entrySet()) {
            asyncMap.addPlayer(fakePlayer.getKey().toString(), fakePlayer.getValue().toString());
            Player asyncPlayer = asyncMap.getPlayer(fakePlayer.getKey().toString());
            TTPGPlayer ttpgPlayer = ttpgMap.getPlayers().get(index);

            //PLAYER STATS
            asyncPlayer.setFaction(AliasHandler.resolveFaction(ttpgPlayer.getFactionShort().toLowerCase()));
            asyncPlayer.setColor(AliasHandler.resolveColor(ttpgPlayer.getColorActual().toLowerCase()));
            asyncPlayer.setCommodities(ttpgPlayer.getCommodities());
            asyncPlayer.setCommoditiesTotal(ttpgPlayer.getMaxCommodities());
            asyncPlayer.setTg(ttpgPlayer.getTradeGoods());
            asyncPlayer.setTacticalCC(ttpgPlayer.getCommandTokens().getTactics());
            asyncPlayer.setFleetCC(ttpgPlayer.getCommandTokens().getFleet());
            asyncPlayer.setStrategicCC(ttpgPlayer.getCommandTokens().getStrategy());



            //SCORED OBJECTIVES
            for (Entry<String, Integer> revealedObjective : asyncMap.getRevealedPublicObjectives().entrySet()) {
                for (String ttpgScoredObjective : ttpgPlayer.getObjectives()) {
                    if (AliasHandler.resolvePublicObjective(ttpgScoredObjective).equalsIgnoreCase(revealedObjective.getKey())) {
                        asyncMap.scorePublicObjective(asyncPlayer.getUserID(),revealedObjective.getValue());
                    }
                }
            }

            //PLANETS
            for (String planet : ttpgPlayer.getPlanetCards()) {
                asyncPlayer.addPlanet(AliasHandler.resolvePlanet(planet.toLowerCase()));
            }

            //LEADERS
            if (!asyncPlayer.getFaction().equals("keleres") || !asyncPlayer.getFaction().equals("nomad")) { //deal with these chumps later
                asyncPlayer.getLeader("agent").setLocked(ttpgPlayer.getLeaders().getAgent().equals("unlocked") ? false : true);
                asyncPlayer.getLeader("commander").setLocked(ttpgPlayer.getLeaders().getCommander().equals("unlocked") ? false : true);
                asyncPlayer.getLeader("hero").setLocked(ttpgPlayer.getLeaders().getHero().equals("unlocked") ? false : true);
            }

            //CUSTODIAN POINTS
            Integer ttpgCustodianPoints = ttpgPlayer.getCustodiansPoints();
            if (ttpgCustodianPoints > 0) {
                while (ttpgCustodianPoints > 0) {
                    asyncMap.scorePublicObjective(asyncPlayer.getUserID(), 0);
                    ttpgCustodianPoints--;
                }
            }

            //TECHS
            for (String technology : ttpgPlayer.getTechnologies()) {
                asyncPlayer.addTech(AliasHandler.resolveTech(technology.toLowerCase()));
            }
            
            index++;
        }
        // setScoredPublicObjectives(ttpgMap.getObjectives().getPublicObjectivesI());

        //TILES - HEX SUMMARY
        String[] hexSummary = ttpgMap.getHexSummary().split(",");
        for (String hex : hexSummary) {
            System.out.println("Hex: " + hex);
            if (hex.length() > 0) {
                Tile tile = ConvertTTPGHexToAsyncTile(asyncMap, hex);
                if (tile != null) {
                    asyncMap.setTile(tile);
                } else {
                    System.out.println("null tile");
                }
            } else {
                System.out.println("0 length hex string");
            }
        }

        //add control tokens to all owned planets
        for (Tile tile : asyncMap.getTileMap().values()) {
            for (UnitHolder unitHolder : tile.getUnitHolders().values()) {
                for (Player player : asyncMap.getPlayers().values()) {
                    for (String planet : player.getPlanets()) {
                        // System.out.println(unitHolder.getName() + "  " + planet + "   " + player.getColor());
                        if (unitHolder.getName().equalsIgnoreCase(planet)) {
                            tile.addControl(Mapper.getControlID(player.getColor()), planet);
                        }
                    }
                }
            }
        }

        return asyncMap;
    }
   
    public static Tile ConvertTTPGHexToAsyncTile (Map asyncMap, String ttpgHex) {
        // System.out.println(" Examining hex summary:  " + ttpgHex);

        // TILE +-X +-Y SPACE ; PLANET1 ; PLANET2 ; ...
        Pattern firstRegionPattern = Pattern.compile("^([0-9AB]+)([-+][0-9]+)([-+][0-9]+)(.*)?$");
        Pattern rotPattern = Pattern.compile("^(\\d+)([AB])(\\d)$"); //ignore hyperlanes for now
        Pattern regionAttachmentsPattern = Pattern.compile("^(.*)\\*(.*)$");  
        
        Matcher matcher = firstRegionPattern.matcher(ttpgHex);
        if (matcher.find()) {
            // System.out.println("     Matches!");
            // System.out.println("       group(0):" + matcher.group(0));
            System.out.println("     TileID:" + matcher.group(1));
            System.out.println("     X:" + matcher.group(2));
            System.out.println("     Y:" + matcher.group(3));
            // System.out.println("       group(4):" + matcher.group(4));
            
        } else {
            System.out.println("     No Match");
            return null;
        }
        
        Tile tile = null;
        String tileID = AliasHandler.resolveTile(matcher.group(1));
        String ttpgXPosition = matcher.group(2);
        String ttpgYPosition = matcher.group(3);
        String ttpgPosition = ttpgXPosition + ttpgYPosition;

        String asyncPosition = AliasHandler.resolveTTPGPosition(ttpgPosition);


        //Handle special cases, tiles to go in TL/TR/BL/BR
        switch (tileID) {
            //TODO: smart placement of mallice/whdelta/nombox
            case "82" -> { //Mallice
                tileID = "82b"; //TODO: If 82 hasunits or control, then 82b, otherwise, 82a
                asyncPosition = "tl"; //hardcode top left for now
            }
            case "51" -> { //Creuss
                //TODO: move DeltaWH if exists in tileList
                asyncPosition = "tr"; //hardcode top right for now
            }
            case "17" -> { //DeltaWH
                //TODO: move Creuss if exists in tileList - i.e. if 17 is near BL, put 51 in BL
            }
            case "54" -> { //Cabal, add S11 cabal prison nearby - i.e. if 54 is near BR, put S11 in BR
                Tile prison = new Tile("S11", "br"); //hardcode bottom right for now
                asyncMap.setTile(prison);
            }
        }

        if (asyncPosition == null) {
            System.out.println("    Could not map: " + ttpgPosition);
            return tile;
        }
        
        
        tile = new Tile(tileID, asyncPosition);
        String tileContents = matcher.group(4);

        Integer index = 0;
        String[] regions = tileContents.split(";");
        System.out.print(regions.length);




        //PER REGION/PLANET/UNITHOLDER
        for (String regionContents : regions) {
            Boolean regionIsSpace = index == 0 ? true : false;
            Boolean regionIsPlanet = index > 0 ? true : false;

            String planetAlias = tileID + "_" + index; //unique planet ID in planet_alias.properties
            String planet = AliasHandler.resolvePlanet(planetAlias);



            if (regionIsSpace) {
                System.out.println("     spaceContents: " + regionContents);
            } else {
                System.out.println("     planet: " + planetAlias + ": " + planet);
                System.out.println("         contents: " + regionContents);
            }
            
            //Find attachments, and split off region
            Matcher matcherAttachments = regionAttachmentsPattern.matcher(regionContents);
            Boolean hasAttachments = matcherAttachments.find();
            String attachments = null;
            System.out.println("         hasAttachments: " + hasAttachments.toString());
            if (hasAttachments) {
                regionContents = matcherAttachments.group(1);
                attachments = matcherAttachments.group(2);
                for (Character attachment : attachments.toCharArray()) {
                    String attachmentResolved = AliasHandler.resolveTTPGAttachment(Character.toString(attachment));
                    System.out.println("          - " + attachment + ": " + attachmentResolved);

                    String tokenFileName = Mapper.getTokenID(attachmentResolved);
                    String attachmentFileName = Mapper.getAttachmentID(attachmentResolved);

                    if (tokenFileName != null) {
                        tile.addToken(tokenFileName, planet);
                    } else if (attachmentFileName != null) {
                        tile.addToken(attachmentFileName, planet);
                    } else {
                        System.out.println("          - " + attachmentResolved + " could not be added - not found");
                    }
                }
            }

            String colour = "";
            Integer regionCount = 1;



            //DECODE REGION STRING, CHAR BY CHAR
            for (int i = 0; i < regionContents.length(); i++) {
                Character chr = regionContents.charAt(i);
                String str = Character.toString(chr);

                if (validColours.contains(str)) { //is a new Color, signify a new set of player's units //MAY ALSO BE AN ATTACHMENT???
                    //reset colour & count
                    colour = AliasHandler.resolveColor(str.toLowerCase());
                    regionCount = 1;

                    System.out.println("            player: " + colour);

                } else if (Character.isDigit(chr)) { // is a count, signify a new group of units
                    System.out.println("                count: " + str);
                    regionCount = Integer.valueOf(str);

                } else if (Character.isLowerCase(chr) && validUnits.contains(str)) { // is a unit, control_token, or CC
                    if (!colour.equals("")){ //colour hasn't shown up yet, so probably just tokens in space, skip unit crap
                        if (str.equals("t")) { //CC
                            tile.addCC(Mapper.getCCID(colour));
                        } else if (str.equals("o")) { //control_token
                            tile.addToken(Mapper.getControlID(colour), AliasHandler.resolvePlanet(planetAlias));
                        } else { // is a unit
                            System.out.println("                unit:  " + AliasHandler.resolveTTPGUnit(str));
                            String unit = AliasHandler.resolveTTPGUnit(str);
                            
                            
                            String unitID = Mapper.getUnitID(unit, colour);
                            String unitCount = String.valueOf(regionCount);
                            
                            if (regionIsSpace) {
                                tile.addUnit("space", unitID, unitCount);
                            } else if (regionIsPlanet) {
                                tile.addUnit(AliasHandler.resolvePlanet(planetAlias), unitID, unitCount);
                            }
                            
                        }
                    }

                } else if (validAttachments.contains(str)) { //attachments that were there that didn't match the RegEx above
                    if (str.equals("e")) { //frontier token
                        System.out.println("attempt to add frontier token to " + tile.getPosition());
                        // tile.addToken(Mapper.getTokenPath(Constants.FRONTIER), Constants.SPACE);
                        AddToken.addToken(null, tile, Constants.FRONTIER, null);
                    }
                } else {
                    System.out.println("                character not recognized:  " + str);
                }
            }

            index++; //next Region/Planet/UnitHolder
        }

        //String color = Helper.getColor(activeMap, event);
        // String unitID = Mapper.getUnitID(unit, color);

        // if (matcher2.find()) {
        //     System.out.println("     Matches!");
        //     for (int i = 0; i < matcher.groupCount()-1; i++) {
        //         System.out.println("       group(" + i + "):" + matcher2.group(i));
        //     }          
        // } else {
        //     System.out.println("     No Match");
        // }
    
        return tile;
    }

    // private static String parseRegion() {

    // }


    public static String currentDateTime() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuuMMddHHmmss" ));   
    }

    public static TTPGMap getTTPGMapFromJsonFile(String filePath) throws Exception {
        String jsonSource = readFileAsString(filePath);
        JsonNode node = parse(jsonSource);

        // System.out.println(generateString(node,true));

        TTPGMap ttpgMap = fromJson(node, TTPGMap.class);
        return ttpgMap;
    }


    public static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    public static JsonNode parse(String source) throws JsonMappingException, JsonProcessingException {
        return objectMapper.readTree(source);
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static ObjectMapper getDefaultObjectMapper(){

        ObjectMapper defaultObjectMapper = new ObjectMapper();
        //CONFIGS
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // defaultObjectMapper.configure(DeserializationFeature.fail, false);
        //END CONFIGS
        return defaultObjectMapper;
    }

    public static <A> A fromJson(JsonNode node, Class<A> clazz) throws JsonProcessingException, IllegalArgumentException {
        return objectMapper.treeToValue(node, clazz);
    }

    public static JsonNode toJson(Object a) {
        return objectMapper.valueToTree(a);
    }

    public static String generateString(JsonNode node) throws JsonProcessingException {
        return generateString(node, false);
    }

    public static String generatePrettyString(JsonNode node, Boolean prettyPrint) throws JsonProcessingException {
        return generateString(node, true);
    }

    public static String generateString(JsonNode node, Boolean prettyPrint) throws JsonProcessingException {
        ObjectWriter objectWriter = objectMapper.writer();
        if (prettyPrint)
            objectWriter = objectWriter.with(SerializationFeature.INDENT_OUTPUT);     
        return objectWriter.writeValueAsString(node);
    }
}
