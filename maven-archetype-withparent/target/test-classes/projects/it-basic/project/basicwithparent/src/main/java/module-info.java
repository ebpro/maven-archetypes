/**
 * A simple jigsaw module
 */
module it.pkg.basicwithparent {
    requires java.logging;
    requires static lombok;

    exports it.pkg;
}
