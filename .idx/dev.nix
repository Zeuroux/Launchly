{ pkgs, ... }: {

  # Which nixpkgs channel to use.
  channel = "stable-23.11"; # or "unstable"

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.nodejs_18
    pkgs.jdk17
    pkgs.kotlin
    pkgs.neofetch
    pkgs.android-tools
  ];


  # Sets environment variables in the workspace
  env = {
    SOME_ENV_VAR = "hello";
    ANDROID_HOME = "/home/user/android_sdk";
  };
  
  # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"

  # Enable previews and customize configuration
  
}