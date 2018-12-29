package ch.epfl.imhof.paintaing;

import ch.epfl.imhof.Attributed;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;

public final class RoadPainterGenerator {
    private RoadPainterGenerator() { }

    public static Painter painterForRoads(RoadSpec spec, RoadSpec... others) {
        List<Painter> netWorkPainters = new ArrayList<>();
        List<RoadSpec> specs = new ArrayList<>();

        specs.add(spec);
        specs.addAll(asList(others));

        specs.forEach(s -> netWorkPainters.add(s.innerBridge));
        specs.forEach(s -> netWorkPainters.add(s.outerBridge));
        specs.forEach(s -> netWorkPainters.add(s.innerRoad));
        specs.forEach(s -> netWorkPainters.add(s.outerRoad));
        specs.forEach(s -> netWorkPainters.add(s.tunnels));

        return netWorkPainters.stream().reduce(Painter::above).get();
    }

    public static final class RoadSpec {
        private static final Predicate<Attributed<?>> isBridge = Filters.tagged("bridge");
        private static final Predicate<Attributed<?>> isTunnel = Filters.tagged("tunnel");
        private static final Predicate<Attributed<?>> isRoad = (isBridge.or(isTunnel)).negate();

        private final Painter innerBridge;
        private final Painter outerBridge;
        private final Painter innerRoad;
        private final Painter outerRoad;
        private final Painter tunnels;

        public RoadSpec(Predicate<Attributed<?>> predicate,
                             float innerWidth,
                             Color innerColor,
                             float outerWidth,
                             Color outerColor) {

            LineStyle innerBridgeStyle = new LineStyle(
                    innerWidth,
                    innerColor,
                    LineStyle.LineCap.Round,
                    LineStyle.LineJoin.Round);
            LineStyle outerBridgeStyle = new LineStyle(
                    innerWidth + 2 * outerWidth,
                    outerColor,
                    LineStyle.LineCap.Butt,
                    LineStyle.LineJoin.Round);

            this.innerBridge = Painter.line(innerBridgeStyle)
                    .when(predicate.and(isBridge));
            this.outerBridge = Painter.line(outerBridgeStyle)
                    .when(predicate.and(isBridge));
            this.innerRoad = Painter.line(innerBridgeStyle)
                    .when(predicate.and(isRoad));
            this.outerRoad = Painter.line(outerBridgeStyle.withCap(LineStyle.LineCap.Round))
                    .when(predicate.and(isRoad));
            this.tunnels = Painter.line(outerBridgeStyle
                    .withStroke(innerWidth / 2f)
                    .withPattern(new float[] { 2 * innerWidth, 2 * innerWidth }))
                    .when(predicate.and(isTunnel));


        }

    }
}
