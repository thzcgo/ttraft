package com.thzc.ttraft.core.node.role;

import java.io.File;

public class NormalLogDir extends AbstractLogDir {

    NormalLogDir(File dir) {
        super(dir);
    }

    @Override
    public String toString() {
        return "NormalLogDir{" +
                "dir=" + dir +
                '}';
    }

}
