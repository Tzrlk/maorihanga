{
	pkgs ? import <nixpkgs> {},
}: with pkgs; mkShellNoCC {
	buildInputs = [
		detekt
		gitMinimal
		kotlin
		kotlin-native
		kotlin-language-server
		kotlin-interactive-shell
	];
	shellHook = lib.concatStringsSep "\n" [
		"echo \"It worked!\""
	];
}
