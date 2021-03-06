package com.pokebattler.fight.resources;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.leandronunes85.etag.ETag;
import com.pokebattler.fight.calculator.CachingRankingSimulator;
import com.pokebattler.fight.calculator.Formulas;
import com.pokebattler.fight.data.PokemonDataCreator;
import com.pokebattler.fight.data.proto.FightOuterClass.AttackStrategyType;

@Component
@Path("/rankings")
public class RankingResource {

    @Resource
    CachingRankingSimulator simulator;
    @Resource
    Formulas formulas;
    @Resource
    PokemonDataCreator creator;

    public static final String MAX_LEVEL = "40";
    public static final int MAX_INDIVIDUAL_STAT = 15;
    public Logger log = LoggerFactory.getLogger(getClass());

    @GET
    @Path("attackers/levels/{attackerLevel}/defenders/levels/{defenderLevel}/strategies/{attackStrategy}/{defenseStrategy}")
    @Produces("application/json")
    @ETag
    public Response rankAttacker(@PathParam("attackerLevel") String attackerLevel,
            @PathParam("defenderLevel") String defenderLevel,
            @PathParam("attackStrategy") AttackStrategyType attackStrategy,
            @PathParam("defenseStrategy") AttackStrategyType defenseStrategy) {
        log.debug("Calculating rankings for attackerLevel {}, defenderLevel {}, attackStrategy {}, defenseStrategy {}", attackerLevel, defenderLevel, attackStrategy,
                defenseStrategy);
        // set caching based on wether the result is random
        // TODO: refactor this to strategy pattern or change to a parameter?
        // maybe a query parameter to seed the rng?
        final javax.ws.rs.core.CacheControl cacheControl = new javax.ws.rs.core.CacheControl();
        cacheControl.setMaxAge(isRandom(attackStrategy, defenseStrategy) ? 0 : 86000);
        return Response.ok(simulator.rank(attackerLevel, defenderLevel, attackStrategy, defenseStrategy)).cacheControl(cacheControl).build();

    }

    private boolean isRandom(AttackStrategyType attackStrategy, AttackStrategyType defenseStrategy) {
        return defenseStrategy == AttackStrategyType.DEFENSE_RANDOM;

    }

}
