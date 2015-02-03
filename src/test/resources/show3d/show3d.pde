/*
 * Copyright (C) 2014-2015 Stichting Mapcode Foundation (http://www.mapcode.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Set these option to what you prefer.
boolean OPTION_DRAW_LAT_LON = true;    // Draw 2D plane of lat/lons as well.
boolean OPTION_STAR_SHAPE = true;      // Draw stars insteads of points.

// Visualization related data.
final int C = 150;                          // Size of sphere.
final int D = 1;                            // Size of star shape.
final float SPEED_RANGE = 4;                // Speed factor to bounce between [1/D, D].

// Camera data.
final float ROT_X_FACTOR = 0.01;            // Rotation factors for X, Y and Z.
final float ROT_Y_FACTOR = 0.0016;
final float ROT_Z_FACTOR = 0.023;

float posX;                                 // Camera location.
float posY;
float posZ;
float rotX = 1.2;                           // Camera rotation.
float rotY = 0;
float rotZ = 0.9;
float speed = 1.0;
float delta = 1.03;
boolean move = true;                        // Move camera or not.

// Data set related data.
final int N = 1000000;                      // Max. number of data points.
float[] lat = new float[N];                 // Lat/lon data.
float[] lon = new float[N];

float[] px = new float[N];                  // X, Y, Z data.
float[] py = new float[N];
float[] pz = new float[N];

int showNumberOfPoints;                     // Number of data points to show now.
int totalNumberOfPoints;                    // Total number of data points.
int addNumberOfPointsPerDraw;

/**
 * The setup() method is called at the start of the program, once.
 */
void setup() {
  size(650, 550, P3D);

  // Set-up initial camera position.
  posX = width / 2.0;
  posY = height / 2.0;
  posZ = 0.0;

  // Choose data file.
  showNumberOfPoints = 0;
  totalNumberOfPoints = 0;
  addNumberOfPointsPerDraw = 1;
}

/**
 * The draw() method is called for every frame.
 */
void draw() {
  clear();

  // Fix number of data points.
  if (showNumberOfPoints < totalNumberOfPoints) {
    showNumberOfPoints = showNumberOfPoints + addNumberOfPointsPerDraw;
  }
  if (showNumberOfPoints > totalNumberOfPoints) {
    showNumberOfPoints = totalNumberOfPoints;
  }

  // Show some basic help.
  fill(255, 255, 255);
  text("MAPCODE VISUALIZATON TOOL", 0, 15);
  fill(0, 200, 200);
  text("Press 'F' to load a new data file", 0, 35);
  text("Press space to show all data at once", 0, 50);
  text("Press mouse button to start/stop animation", 0, 65);
  fill(255, 200, 255);
  text("Showing " + showNumberOfPoints + " data points", 0, 85);

  // Move the camera (in fact, the entire world in moved except the camera
  // because otherwise the text would not be visible anymore).
  translate(posX, posY, posZ);
  rotateX(rotX);
  rotateY(rotY);
  rotateZ(rotZ);

  // Draw sphere.
  stroke(30, 30, 200, 50);
  fill(30, 30, 200, 50);
  sphere(C * 0.95);

  // Draw poles.
  stroke(200, 50, 0, 255);
  line(0, 0, -C - 300, 0, 0, 0);
  line(0, 1, -C - 300, 0, 1, 0);
  line(1, 0, -C - 300, 1, 0, 0);
  fill(200, 50, 0, 255);
  text("SOUTH", 0, 0, -C - 5);

  stroke(0, 200, 50, 255);
  line(0, 0, 0, 0, 0, C + 300);
  line(0, 1, 0, 0, 1, C + 300);
  line(1, 0, 0, 1, 0, C + 300);
  fill(0, 200, 50, 255);
  text("NORTH", 0, 0, C + 5);

  // Draw sphere dots.
  stroke(255, 255, 255, 200);
  for (int i = 0; i < showNumberOfPoints; ++i) {
    float x = C * px[i];
    float y = C * py[i];
    float z = C * pz[i];
    if (OPTION_STAR_SHAPE) {
      line(x - D, y, z, x + D, y, z);
      line(x, y - D, z, x, y + D, z);
    }
    line(x, y, z - D, x, y, z + D);
  }

  if (OPTION_DRAW_LAT_LON) {

    // Draw lat/lon bounds.
    stroke(0, 255, 255, 200);
    line(C + 5, -C - 50, C + 5, C + 5, -C - 50, -C - 5);
    line(C + 5, -C - 50, -C - 5, -C - 5, -C - 50, -C - 5);
    line(-C - 5, -C - 50, -C - 5, -C - 5, -C - 50, C + 5);
    line(-C - 5, -C - 50, C + 5, C + 5, -C - 50, C + 5);

    // Draw lat/lon dots.
    stroke(255, 255, 0, 200);
    for (int i = 0; i < showNumberOfPoints; ++i) {
      float x = C * lon[i];
      float y = -C - 51;
      float z = C * lat[i];
      if (OPTION_STAR_SHAPE) {
        line(x - D, y, z, x + D, y, z);
        line(x, y - D, z, x, y + D, z);
      }
      line(x, y, z - D, x, y, z + D);
    }
  }

  if (move) {
    rotX += ROT_X_FACTOR * speed;
    rotY -= ROT_Y_FACTOR * speed;
    rotZ += ROT_Z_FACTOR * speed;
    speed = speed * delta;
    if ((speed < (1.0 / SPEED_RANGE)) || (speed > SPEED_RANGE)) {
      delta = 1.0 / delta;
    }
  }
}

void loadDataFile(String fileName) {
  println("Load data file: " + fileName);
  BufferedReader reader = createReader(fileName);
  if (reader == null) {
    println("ERROR: Data file " + fileName + " not found... please check file name");
    return;
  }

  totalNumberOfPoints = 0;
  showNumberOfPoints = 0;
  int nr = 0;
  try {
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }

      // Parse line.
      String[] items = split(line, " ");
      int nrItems = Integer.parseInt(items[0]);
      lat[nr] = Float.parseFloat(items[1]) / 90.0;
      lon[nr] = Float.parseFloat(items[2]) / 180.0;
      px[nr] = Float.parseFloat(items[3]);
      py[nr] = Float.parseFloat(items[4]);
      pz[nr] = Float.parseFloat(items[5]);

      // Skip codes.
      for (int i = 0; i < nrItems; ++i) {
        reader.readLine();
      }
      reader.readLine();
      ++nr;
    }
  }
  catch (IOException e) {
    println("Error reading file: " + fileName, e);
  }
  finally {
    try {
      reader.close();
    }
    catch (IOException e) {
      println("Error closing file: " + fileName, e);
    }
  }
  addNumberOfPointsPerDraw = Math.max(1, nr / 10);
  totalNumberOfPoints = nr;
  OPTION_STAR_SHAPE = (totalNumberOfPoints <= 100000);
  println("Data file read, totalNumberOfPoints=" + totalNumberOfPoints);
}

/**
 * The method fileSelected() is the call back for selectInput().
 */
void fileSelected(File file) {
  if (file == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    loadDataFile(file.getAbsolutePath());
  }
}

/**
 * The method keyPressed() is a call back for key presses.
 */
void keyPressed() {
  if ((key == 'f') || (key == 'F')) {
    selectInput("Select a data file to load:", "fileSelected");
  }
  else if (key == ' ') {
    showNumberOfPoints = totalNumberOfPoints;
  }
}

/**
 * The mouseReleased() method is called when the mouse button is released.
 * It stops and starts the camera movemement.
 */
void mouseReleased() {
  move = !move;
}

