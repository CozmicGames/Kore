# Kore

**Kore** is a platform independant framework aimed towards game development, written as a multiplatform Kotlin project.
It provides easy cross-platform low level access to:

- Application management
- Input
- Files
- Memory
- Graphics
- Audio
- Numerous utility functions and classes

**This is very much in development, so expect breaking changes!**

### Application management

Just a few lines are needed to start an application and listening to it's lifecycle:

    val configuration = Configuration()
    
    Kore.start(object : Application {
      override fun onCreate() { /* called once on creation */ }
      
      override fun onResize(width: Int, height: Int) { /* called once on application resize */ }
      
      override fun onFrame(delta: Float) { /* called every frame */ }
      
      override fun onDispose() { /* called once on shutdown */ }
    }, configuration) { DesktopPlatform() /* just desktop supported for now */ }
    
Additionally, you can configure **Kore** before calling the 'start' function:

    configuration.title = "Example title"
    configuration.vsync = false
    configuration.width = 123
    configuration.height = 456
    
    // And so on...

### Input

Easy access to input events is provided for:
* Touch input
* Mouse input
* Keyboard input (both key stroke and typing)
* Gamepad input (not yet tested)

The current input state is also provided to access.

### Files

The framework provides input and output streams, which can be used to read and write data. More complex functionality should be provided by additional libraries.

### Memory

Access to native memory is provided via 'Memory' and used throughout the framework.

### Graphics

Access to all vital information for graphics related stuff is provided (surface size, vsync, safe insets etc.).
It also provides functionality to load, save and work with images and load fonts, either from a provided TTF file or a system font.
To provide functionality to draw stuff on screen, **Kore** utilizes an abstracted graphics API, taking inspiration from various sources and providing a subset of what targeted platforms should be able to support.
It consists of:
* Textures (2D, 3D and Cube textures)
* Vertex and index buffers
* Uniform buffers
* Pipelines (combining shader and graphics state)
* Framebuffers

Pipelines can be created either in code, by setting different states, the input vertex layout and program sources, or via a 'PipelineDefinition', which can be loaded from a file or parsed from a string. The contained program sources follow GLSL syntax.

Pipeline definitions consist of different sections, each beginning with '#section' and their name (example below):
- Layout: Describes the input vertex layout
- State: Describes the pipeline state
- Types: Describe type definitions
- Uniforms: Describe uniform values used by the program
- Common: Common source code, which is used in any program stage
- Vertex: Vertex stage source, this is the only section that must be present
- Geometry: Geometry stage source
- Fragment: Fragment stage source
- Compute: Compute stage source, if this is used, layout, state, vertex, geometry and fragment sections are not needed

A pipeline definition can be created as such:

    #section state
    blend add source_alpha one_minus_source_alpha //This would be alpha blending
    cull back 
    depth less
    colormask true true true true
    depthmask true
    stencilmask 0xFF //stencil values have to be represented in hey format
    stencil always 0xFF 0xFF keep keep keep
    
    #section layout
    vec3 position
    vec2 texcoords
    normalized vec4 color byte //This would be a normalized vec4 which components are represented in memory as single bytes
    packed { 
    /*
      Supports packed components, unpacking is automatically added, so no need for any boilerplate code
      Packed components are always packed in a single 32 bit integer
    */
        int foo 4 //An integer component value packed into 4 bits
        ivec2 bar 8 //An integer vector with two components, where each component takes up 8 bits
    }
    
    #section types
    struct foo {
        vec2 a
    }
    struct bar {
        foo b
    }
    
    #section uniforms
    bool enabled
    vec2 dimensions
    sampler2D image
    sambler3D volume
    samplerCube cube
    image2D image1 rgba8_unorm     // Only accessible if compute shaders are supported
    image3D image2 rgba32_float    // Only accessible if compute shaders are supported  
    
    #section common
    float doCommonStuff() {
        return 1.0;
    }
    
    #section vertex
    out passColor;
    
    void main() {
        gl_Position = vec4(position, 1.0);
        passColor = color;
    }
    
    #section geometry
    // Geometry shader code goes here
    
    #section fragment
    in passColor;
    out outColor;
    
    void main() {
        outColor = passColor;
    }
    
    #section compute
    NUM_THREADS(1, 1, 1) //Set the workgroup sizes
    void main() {
        
    } 

### Audio

Provides loading WAV and MP3 files and playing sounds.

## Building

Just import the provided files as a gradle project and you're good to go.

For now only a desktop implementation is provided, which runs on Windows, Mac and Linux (the latter two untested, though).
More platforms are planned for future releases.
