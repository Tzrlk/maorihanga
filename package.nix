{
	pkgs ? import <nixpkgs> {},
}: let
	inherit (pkgs) lib;

	joinString = lib.concatStringsSep;

	filterGitignore = pkgs.nix-gitignore.gitignoreFilterSource (_: _: true) [
		# TODO: More gitignore-style exclusion paths.
	];

	# takeUntil: takes elements from a list until the predicate is true (inclusive or exclusive)
	takeUntil = pred: list:
		if list == [] then
			[]
		else let
			head = (builtins.head list);
		in if pred head then
			[] # stop before the matching element
		else
			[ head ] ++ takeUntil pred (builtins.tail list);

	skipUntil = pred: list:
		if list == [] then
			[]
		else if pred (builtins.head list) then
			list
		else
			skipUntil pred (builtins.tail list);

	readme = let

		# Use pandoc to parse the markdown readme file.
		json = pkgs.runCommand "parse-readme"
			{ pandoc = "${pkgs.pandoc}/bin/pandoc"; readme = ./README.md; }
			"$pandoc -f markdown -t json -o $out $readme";

		# Parse the output json into nix.
		ast = builtins.fromJSON
			(builtins.readFile json);

		pandocTypes = {
			"Str"   = (item: item.c);
			"Space" = (item: " ");
		};

		# Turns a pandoc json paragraph back into text.
		paraText = para:
			(lib.concatStringsSep ""
				(builtins.map
					(item:
						(pandocTypes.${item.t} or (item: "¿"))
						item)
					para.c));

		# All intro paragraphs, joined back into strings.
		paras = builtins.map paraText
			(takeUntil
				(block: block.t != "Para")
				(skipUntil
					(block: block.t == "Para")
					ast.blocks));

	in {
		description     = builtins.head paras;
		longDescription = joinString "\n" (builtins.tail paras);
	};

in pkgs.stdenv.mkDerivation {
	pname   = "maorihanga";
	version = "0.0.0";
	meta    = {
		inherit (readme) description longDescription;
		homepage         = "https://tzrlk.aetheric.co.nz/maoritanga";
		sourceProvenance = [ lib.sourceTypes.fromSource ];
		branch           = "main";
		license          = lib.licenses.lgpl3Plus;
		platforms        = lib.platforms.all;
		mainProgram      = "maorihanga.kts";
		maintainers      = [{
			name     = "Peter Cummuskey";
			email    = "peterc@aetheric.co.nz";
			github   = "Tzrlk";
			githubId = 169667;
		}];
	};

	src = filterGitignore ./.;

	# Compile-only dependencies.
	nativeBuildInputs = with pkgs; [
	];

	# Runtime dependencies
	buildInputs = with pkgs; [
		kotlin
	];

	# Test dependencies
	nativeCheckInputs = with pkgs; [
	];

	# No build phase (yet)
	dontBuild = true;

	installPhase = joinString "\n" [
		"mkdir -p $out"
		"cp $src/maorihanga.kts $out/"
	];

	# Functional tests
	passthru.tests = {
		version = pkgs.testers.testVersion { package = pkgs.maorihanga; };
	};

}
