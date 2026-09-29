/**
 * A simple jigsaw module
 */
module ${package} {
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires static lombok;

    exports ${package};
}
