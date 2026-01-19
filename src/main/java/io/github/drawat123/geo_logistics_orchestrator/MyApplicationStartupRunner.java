package io.github.drawat123.geo_logistics_orchestrator;

import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.CityGraphService;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.PathfinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationStartupRunner implements CommandLineRunner {
    private final CityGraphService cityGraphService;

    private final PathfinderService pathfinderService;

    @Autowired
    public MyApplicationStartupRunner(CityGraphService cityGraphService, PathfinderService pathfinderService) {
        this.cityGraphService = cityGraphService;
        this.pathfinderService = pathfinderService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Executing tasks after application startup...");

        /*
            A(10,74) -> [( B(11,34), 5 ), ( C(8,10), 2 )]
            B(11,34) -> [( D(48,30), 4 )]
            C(8,10) -> [(D(48,30), 6), (E(81,63), 3)]
        */
        LocationNode l1 = new LocationNode("A", 10, 74);
        LocationNode l2 = new LocationNode("B", 11, 34);
        LocationNode l3 = new LocationNode("C", 8, 10);
        LocationNode l4 = new LocationNode("D", 48, 30);
        LocationNode l5 = new LocationNode("E", 81, 63);

        cityGraphService.addLocation(l1);
        cityGraphService.addLocation(l2);
        cityGraphService.addLocation(l3);
        cityGraphService.addLocation(l4);
        cityGraphService.addLocation(l5);

        cityGraphService.addRoad(l1.id(), l2.id(), 5);
        cityGraphService.addRoad(l1.id(), l3.id(), 2);
        cityGraphService.addRoad(l2.id(), l4.id(), 4);
        cityGraphService.addRoad(l3.id(), l4.id(), 6);
        cityGraphService.addRoad(l3.id(), l5.id(), 3);

        PathResult pathResult = pathfinderService.findShortestPath(cityGraphService, l1.id(), l4.id());

        System.out.println("Path: " + pathResult.path());
        System.out.println("Distance: " + pathResult.totalDistance());
    }
}
