package com.esprit.ffw.signalement.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nature de l'observation : FUMEE, FEU ou BRAISES")
public enum TypeSignalement { FUMEE, FEU, BRAISES }
