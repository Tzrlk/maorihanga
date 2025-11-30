{
	description = "An application that attempts to translate Romanised Maori text into Hangul characters.";

	inputs = {
		nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
		systems.url = "github:nix-systems/x86_64-linux";
		flake-parts = {
			url = "github:hercules-ci/flake-parts";
		};
	};

	outputs = inputs @ { self, nixpkgs, flake-parts, ... }:
		flake-parts.lib.mkFlake { inherit inputs; } {

			imports = [
			];

			systems = [ "x86_64-linux" ];
			perSystem = { config, self', inputs', pkgs, system, ... }: {

				packages = {
					default    = self'.packages.maorihanga;
					maorihanga = import ./package.nix {
						inherit pkgs;
					};
				};

				apps = {
					default = self'.apps.maorihanga;
					maorihanga = {
						type    = "app";
						program = "${self'.packages.maorihanga}/maorihanga.kts";
					};
				};

				devShells.default = import ./shell.nix {
					inherit pkgs;
				};

			};

			flake = {

				checks = {
					# TODO: Testing.
				};

			};

		};

}
