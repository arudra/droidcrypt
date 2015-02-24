



public class Hugo
{
  
  //INPUTS: input image path, Password
  String inputImage, password;

  // output image path which holds hash of JPEG
  String outputImage; 


  // create hash of JPEG image
  // outputImage = localDIR + hash( inputImage.image );

  //  -> convert to PGM
  // image = PGM.(inputImage.image)


  //  Load Cover
  // create config
  float payload, gamma, sigma;
  int randSeed;
  bool verbose = false;
  unsigned int stc_constr_height = 0;
  cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stc_constr_height, randSeed, message);


  //  -> call function Load_Image from Mat2D and put inside Mat2D variable "cover"

  //  -> call constructor for base_cost_model with cover & config and put inside "model"
  //  Embed Image
  //  -> Mat2D variable: stego = model->Embed(bunch of dummy floats) 
  //  Save stego
  //  -> call Save_Image(stegoPath, stego)












}
