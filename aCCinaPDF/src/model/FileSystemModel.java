/*
 *   Copyright 2015 Luís Diogo Zambujo, Micael Sousa Farinha and Miguel Frade
 *
 *   This file is part of aCCinaPDF.
 *
 *   aCCinaPDF is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   aCCinaPDF is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with aCCinaPDF.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package model;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Diogo
 */
public class FileSystemModel implements TreeModel {

    private final File root;
    private final ArrayList<TreeModelListener> listeners = new ArrayList<>();
    private FilenameFilter ff;

    public FileSystemModel(File rootDirectory, final String match) {
        root = rootDirectory;
        if (match != null) {
            if (!match.isEmpty()) {
                ff = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().contains(match.toLowerCase());
                    }
                };
            } else {
                setDefaultFilter();
            }
        } else {
            setDefaultFilter();
        }
    }

    private void setDefaultFilter() {
        ff = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if ((new File(dir.getAbsolutePath() + System.getProperty("file.separator") + name)).isDirectory()) {
                    return true;
                } else if (name.endsWith(".pdf")) {
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        File directory = (File) parent;

        String[] children = directory.list(ff);

        final ArrayList<String> folders = new ArrayList<>();
        final ArrayList<String> files = new ArrayList<>();

        int num = 0;

        for (String str : children) {
            if (new File(directory.getAbsolutePath() + "\\" + str).isDirectory()) {
                folders.add(str);
            } else {
                files.add(str);
            }
        }

        Comparator<String> strCompare = new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
            }
        };
        if (!folders.isEmpty()) {
            folders.sort(strCompare);
        }
        if (!files.isEmpty()) {
            files.sort(strCompare);
        }

        int len = folders.size() + files.size();
        String[] children2 = new String[len];

        for (String s : folders) {
            children2[num] = s;
            num++;
        }

        for (String s : files) {
            children2[num] = s;
            num++;
        }

        return new TreeFile(directory, children2[index]);
    }

    @Override
    public int getChildCount(Object parent) {
        File file = (File) parent;
        if (file.isDirectory()) {
            String[] fileList = file.list(ff);

            if (fileList != null) {
                return file.list(ff).length;
            }
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        File file = (File) node;
        return file.isFile();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        File directory = (File) parent;
        File file = (File) child;
        String[] children = directory.list();
        for (int i = 0; i < children.length; i++) {
            if (file.getName().equals(children[i])) {
                return i;
            }
        }
        return -1;

    }

    @Override
    public void valueForPathChanged(TreePath path, Object value) {
        File oldFile = (File) path.getLastPathComponent();
        String fileParentPath = oldFile.getParent();
        String newFileName = (String) value;
        File targetFile = new File(fileParentPath, newFileName);
        oldFile.renameTo(targetFile);
        File parent = new File(fileParentPath);
        int[] changedChildrenIndices = {getIndexOfChild(parent, targetFile)};
        Object[] changedChildren = {targetFile};
        fireTreeNodesChanged(path.getParentPath(), changedChildrenIndices, changedChildren);

    }

    private void fireTreeNodesChanged(TreePath parentPath, int[] indices, Object[] children) {
        TreeModelEvent event = new TreeModelEvent(this, parentPath, indices, children);
        Iterator iterator = listeners.iterator();
        TreeModelListener listener = null;
        while (iterator.hasNext()) {
            listener = (TreeModelListener) iterator.next();
            listener.treeNodesChanged(event);
        }
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listeners.remove(listener);
    }

    private class TreeFile extends File {

        public TreeFile(File parent, String child) {
            super(parent, child);
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
