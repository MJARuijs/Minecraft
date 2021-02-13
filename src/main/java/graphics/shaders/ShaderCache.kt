package graphics.shaders

import resources.Cache

object ShaderCache: Cache<Shader>(ShaderLoader())