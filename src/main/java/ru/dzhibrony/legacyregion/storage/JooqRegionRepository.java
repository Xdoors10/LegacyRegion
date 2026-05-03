package ru.dzhibrony.legacyregion.storage;

import com.mefrreex.jooq.database.IDatabase;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionLocation;
import ru.dzhibrony.legacyregion.model.RegionMember;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class JooqRegionRepository implements RegionRepository {

    private static final Table<?> REGIONS = DSL.table("legacy_regions");
    private static final Table<?> MEMBERS = DSL.table("legacy_region_members");
    private static final Field<String> REGION_ID = DSL.field("id", String.class);
    private static final Field<String> LEVEL = DSL.field("level_name", String.class);
    private static final Field<Integer> X = DSL.field("x", Integer.class);
    private static final Field<Integer> Y = DSL.field("y", Integer.class);
    private static final Field<Integer> Z = DSL.field("z", Integer.class);
    private static final Field<Integer> RADIUS = DSL.field("radius", Integer.class);
    private static final Field<String> DEFINITION_KEY = DSL.field("definition_key", String.class);
    private static final Field<String> DISPLAY_NAME = DSL.field("display_name", String.class);
    private static final Field<String> OWNER_NAME = DSL.field("owner_name", String.class);
    private static final Field<String> OWNER_KEY = DSL.field("owner_key", String.class);
    private static final Field<Boolean> HIDDEN = DSL.field("hidden_coordinates", Boolean.class);
    private static final Field<Boolean> MEMBERS_BREAK = DSL.field("members_break_region_block", Boolean.class);
    private static final Field<String> MEMBER_REGION_ID = DSL.field("region_id", String.class);
    private static final Field<String> MEMBER_NAME = DSL.field("player_name", String.class);
    private static final Field<String> MEMBER_KEY = DSL.field("player_key", String.class);

    private final IDatabase database;

    public JooqRegionRepository(IDatabase database) {
        this.database = database;
    }

    @Override
    public void initialize() {
        this.execute(this::createRegionsTable);
        this.execute(this::createMembersTable);
    }

    @Override
    public List<Region> loadRegions() {
        Map<String, Region> regions = this.query(this::fetchRegions);
        this.query(dsl -> this.fetchMembers(dsl, regions));
        return new ArrayList<>(regions.values());
    }

    @Override
    public void saveRegion(Region region) {
        this.execute(dsl -> {
            this.deleteRegionRow(dsl, region.id());
            this.insertRegionRow(dsl, region);
        });
    }

    @Override
    public void deleteRegion(String regionId) {
        this.execute(dsl -> {
            this.deleteMembersByRegion(dsl, regionId);
            this.deleteRegionRow(dsl, regionId);
        });
    }

    @Override
    public void addMember(String regionId, RegionMember member) {
        this.execute(dsl -> {
            this.deleteMemberRow(dsl, regionId, member.key());
            this.insertMemberRow(dsl, regionId, member);
        });
    }

    @Override
    public void removeMember(String regionId, String memberKey) {
        this.execute(dsl -> this.deleteMemberRow(dsl, regionId, memberKey));
    }

    private void createRegionsTable(DSLContext dsl) {
        dsl.createTableIfNotExists(REGIONS)
                .column("id", SQLDataType.VARCHAR(180).nullable(false))
                .column("level_name", SQLDataType.VARCHAR(128).nullable(false))
                .column("x", SQLDataType.INTEGER.nullable(false))
                .column("y", SQLDataType.INTEGER.nullable(false))
                .column("z", SQLDataType.INTEGER.nullable(false))
                .column("radius", SQLDataType.INTEGER.nullable(false))
                .column("definition_key", SQLDataType.VARCHAR(128).nullable(false))
                .column("display_name", SQLDataType.VARCHAR(128).nullable(false))
                .column("owner_name", SQLDataType.VARCHAR(64).nullable(false))
                .column("owner_key", SQLDataType.VARCHAR(64).nullable(false))
                .column("hidden_coordinates", SQLDataType.BOOLEAN.nullable(false))
                .column("members_break_region_block", SQLDataType.BOOLEAN.nullable(false))
                .constraint(DSL.constraint("pk_legacy_regions").primaryKey("id"))
                .execute();
    }

    private void createMembersTable(DSLContext dsl) {
        dsl.createTableIfNotExists(MEMBERS)
                .column("region_id", SQLDataType.VARCHAR(180).nullable(false))
                .column("player_name", SQLDataType.VARCHAR(64).nullable(false))
                .column("player_key", SQLDataType.VARCHAR(64).nullable(false))
                .constraint(DSL.constraint("pk_legacy_region_members").primaryKey("region_id", "player_key"))
                .execute();
    }

    private Map<String, Region> fetchRegions(DSLContext dsl) {
        Map<String, Region> regions = new LinkedHashMap<>();
        Result<org.jooq.Record> rows = dsl.select().from(REGIONS).fetch();
        rows.forEach(row -> this.putRegion(regions, row));
        return regions;
    }

    private Map<String, Region> fetchMembers(DSLContext dsl, Map<String, Region> regions) {
        Result<org.jooq.Record> rows = dsl.select().from(MEMBERS).fetch();
        rows.forEach(row -> this.addMemberFromRow(regions, row));
        return regions;
    }

    private void putRegion(Map<String, Region> regions, org.jooq.Record row) {
        Region region = this.regionFromRow(row);
        regions.put(region.id(), region);
    }

    private Region regionFromRow(org.jooq.Record row) {
        RegionLocation location = this.locationFromRow(row);
        Region owner = new Region(location, row.get(RADIUS), row.get(DEFINITION_KEY), row.get(DISPLAY_NAME),
                new RegionMember(row.get(OWNER_NAME), row.get(OWNER_KEY)));
        owner.hiddenCoordinates(Boolean.TRUE.equals(row.get(HIDDEN)));
        owner.membersCanBreakRegionBlock(Boolean.TRUE.equals(row.get(MEMBERS_BREAK)));
        return owner;
    }

    private RegionLocation locationFromRow(org.jooq.Record row) {
        return new RegionLocation(row.get(LEVEL), row.get(X), row.get(Y), row.get(Z));
    }

    private void addMemberFromRow(Map<String, Region> regions, org.jooq.Record row) {
        Region region = regions.get(row.get(MEMBER_REGION_ID));
        if (region != null) {
            region.addMember(new RegionMember(row.get(MEMBER_NAME), row.get(MEMBER_KEY)));
        }
    }

    private void insertRegionRow(DSLContext dsl, Region region) {
        dsl.insertInto(REGIONS)
                .set(REGION_ID, region.id())
                .set(LEVEL, region.location().levelName())
                .set(X, region.location().x())
                .set(Y, region.location().y())
                .set(Z, region.location().z())
                .set(RADIUS, region.radius())
                .set(DEFINITION_KEY, region.definitionKey())
                .set(DISPLAY_NAME, region.displayName())
                .set(OWNER_NAME, region.ownerName())
                .set(OWNER_KEY, region.ownerKey())
                .set(HIDDEN, region.hiddenCoordinates())
                .set(MEMBERS_BREAK, region.membersCanBreakRegionBlock())
                .execute();
    }

    private void insertMemberRow(DSLContext dsl, String regionId, RegionMember member) {
        dsl.insertInto(MEMBERS)
                .set(MEMBER_REGION_ID, regionId)
                .set(MEMBER_NAME, member.name())
                .set(MEMBER_KEY, member.key())
                .execute();
    }

    private void deleteRegionRow(DSLContext dsl, String regionId) {
        dsl.deleteFrom(REGIONS).where(REGION_ID.eq(regionId)).execute();
    }

    private void deleteMembersByRegion(DSLContext dsl, String regionId) {
        dsl.deleteFrom(MEMBERS).where(MEMBER_REGION_ID.eq(regionId)).execute();
    }

    private void deleteMemberRow(DSLContext dsl, String regionId, String memberKey) {
        dsl.deleteFrom(MEMBERS)
                .where(MEMBER_REGION_ID.eq(regionId))
                .and(MEMBER_KEY.eq(memberKey))
                .execute();
    }

    private void execute(Consumer<DSLContext> action) {
        this.query(dsl -> {
            action.accept(dsl);
            return null;
        });
    }

    private <T> T query(Function<DSLContext, T> action) {
        Connection connection = this.database.getConnection().join();
        return action.apply(DSL.using(connection, this.database.dialect()));
    }
}
