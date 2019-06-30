#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

extern unsigned int color_map[];
int max_iterations = 255;


/* This function (compute_point) is the algorithmic core of this program,
   computing what could be called the Mandelbrot function of the complex
   number (cr, ci).
*/

int compute_point(double ci, double cr) {
  int iterations = 0;
  double zi = 0;
  double zr = 0;
  
  while ((zr*zr + zi*zi < 4) && (iterations < max_iterations)) {
    double nr, ni; 

    /* Z <-- Z^2 + C */
   
    nr = zr*zr - zi*zi + cr;
    ni = 2*zr*zi + ci;
    
    zi = ni;
    zr = nr;

    iterations ++;
  } 
  return iterations;  
}

/* The "compute" function computes the Mandelbrot function over every
   point on a grid that is "nx" points wide by "ny" points tall, where
   (xmin,ymin) and (xmax,ymax) give two corners of the region the 
   complex plane.
*/
void compute(unsigned char *buffer, int nx, int ny, double x1, double y1, 
	     double x2, double y2) {
  double delta_x, delta_y;
  int x, y;

  delta_x = (x2 - x1)/nx;
  delta_y = (y2 - y1)/ny;

  for (y=0;  y<ny; y++) {
    double y_value = y1 + delta_y * y;
    for (x=0; x<nx; x++) {
      double x_value = x1 + delta_x * x;
      buffer[y*nx + x] = compute_point(x_value, y_value);
    }
  }
  
}



/* Output the data contained in the buffer to a Portable Color Map format
   image file.  The parameter "max" should be an upper bound for the
   data values in the buffer. 
*/

void output_pgm(char *filename,unsigned char *buffer, int nx, int ny, int max) {
  int i, ix;
  FILE *file;
  file = fopen(filename,"w");
  if(file == NULL){
	  printf("Error opening file %s\n", filename);
	  exit(1);
  }
  fprintf(file,"P3\n");
  fprintf(file,"%d %d\n",nx,ny);
  fprintf(file,"%d\n",max);
  for (i=0; i<nx*ny; i++) {
    if (!(i%nx)) fprintf(file,"\n");
    ix = 3 * (int)buffer[i];
    fprintf(file,"%d %d %d ", color_map[ix], color_map[ix+1], color_map[ix+2]);
  }
  fclose(file);
}

int main(int argc, char *argv[]){
	int w, l;
	double x1, y1, x2, y2;
	unsigned char *b;

	if(argc != 8){
		printf("Usage: %s x1 y1 x2 y2 W L output_file\n", argv[0]);
		return 1;
	}
	x1 = atof(argv[1]);
	y1 = atof(argv[2]);
	x2 = atof(argv[3]);
	y2 = atof(argv[4]);
	w = atoi(argv[5]);
	l = atoi(argv[6]);
	b = (unsigned char *)malloc(w*l);
	compute( b, w, l, x1, y1, x2, y2);
	output_pgm( argv[7], b, w, l, 255);
	return 0;
}
