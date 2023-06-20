package ti4.model;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TileModel {
    private String id;
    private String name;
    private List<String> aliases;
    private String imagePath;
    private List<String> planetIds;
    private ShipPositionModel.ShipPosition shipPositionsType;
    private List<Point> spaceTokenLocations;
    private Set<WormholeModel.Wormhole> wormholes;

    public TileModel() {}

    public TileModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getNameNullSafe() {
        return Optional.ofNullable(name).orElse("");
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<String> getPlanets() {
        return planetIds;
    }

    public void setPlanets(List<String> planetIds) {
        this.planetIds = planetIds;
    }

    public ShipPositionModel.ShipPosition getShipPositionsType() {
        return shipPositionsType;
    }

    public void setShipPositionsType(ShipPositionModel.ShipPosition shipPositionsType) {
        this.shipPositionsType = shipPositionsType;
    }

    public List<Point> getSpaceTokenLocations() {
        return spaceTokenLocations;
    }

    public void setSpaceTokenLocations(List<Point> spaceTokenLocations) {
        this.spaceTokenLocations = spaceTokenLocations;
    }
    public Set<WormholeModel.Wormhole> getWormholes() {
        return wormholes;
    }

    public void setWormholes(Set<WormholeModel.Wormhole> wormholes) {
        this.wormholes = wormholes;
    }
}