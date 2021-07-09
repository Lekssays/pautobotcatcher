package com.lucalanda.botnetdetectioncontract.model;

import com.owlike.genson.annotation.JsonProperty;
import main.model.MutualContactGraph;

public class SerializableMutualContactGraph extends MutualContactGraph {

    public SerializableMutualContactGraph(@JsonProperty("edges") String[] edges) {
        super(edges);
    }

    public static SerializableMutualContactGraph from(MutualContactGraph graph) {
        String[] edges = graph.getEdges();

        return new SerializableMutualContactGraph(edges);
    }

    public static SerializableMutualContactGraph getEmptyGraph() {
        return new SerializableMutualContactGraph(new String[0]);
    }

}
