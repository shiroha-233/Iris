package net.irisshaders.iris.compat.immersiveportals;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.NamespacedId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理Immersive Portals跨维度渲染时的维度上下文
 * 确保Iris能够正确识别当前正在渲染的维度，从而为DH兼容性系统提供正确的渲染上下文
 */
public class ImmersivePortalsDimensionContext {
    private static final ImmersivePortalsDimensionContext INSTANCE = new ImmersivePortalsDimensionContext();
    
    // 线程本地存储，用于跟踪当前渲染线程正在处理的维度
    private static final ThreadLocal<ResourceKey<Level>> CURRENT_RENDERING_DIMENSION = new ThreadLocal<>();
    
    // 维度渲染深度计数器，用于处理嵌套的跨维度渲染
    private static final ThreadLocal<Integer> RENDERING_DEPTH = ThreadLocal.withInitial(() -> 0);
    
    // 缓存维度ID映射，提高性能
    private final ConcurrentMap<ResourceKey<Level>, NamespacedId> dimensionIdCache = new ConcurrentHashMap<>();
    
    private ImmersivePortalsDimensionContext() {}
    
    public static ImmersivePortalsDimensionContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * 设置当前正在渲染的维度
     * 在IP开始渲染特定维度时调用
     */
    public void setCurrentRenderingDimension(@Nullable ResourceKey<Level> dimension) {
        if (dimension != null) {
            CURRENT_RENDERING_DIMENSION.set(dimension);
            RENDERING_DEPTH.set(RENDERING_DEPTH.get() + 1);
            Iris.logger.debug("IP: Setting rendering dimension to {} (depth: {})", 
                dimension.location(), RENDERING_DEPTH.get());
        } else {
            int depth = RENDERING_DEPTH.get();
            if (depth > 0) {
                RENDERING_DEPTH.set(depth - 1);
                if (depth == 1) {
                    // 回到最外层，清除维度上下文
                    CURRENT_RENDERING_DIMENSION.remove();
                    Iris.logger.debug("IP: Cleared rendering dimension context");
                } else {
                    Iris.logger.debug("IP: Reduced rendering depth to {}", depth - 1);
                }
            }
        }
    }
    
    /**
     * 获取当前正在渲染的维度
     * 如果没有IP上下文，返回null（使用默认行为）
     */
    @Nullable
    public ResourceKey<Level> getCurrentRenderingDimension() {
        return CURRENT_RENDERING_DIMENSION.get();
    }
    
    /**
     * 获取当前渲染维度的NamespacedId
     * 优先使用IP上下文，如果没有则回退到默认行为
     */
    public NamespacedId getCurrentDimensionId() {
        ResourceKey<Level> ipDimension = getCurrentRenderingDimension();
        
        if (ipDimension != null) {
            // 使用缓存提高性能
            return dimensionIdCache.computeIfAbsent(ipDimension, 
                dim -> new NamespacedId(dim.location().getNamespace(), dim.location().getPath()));
        }
        
        // 回退到Iris的默认行为
        return Iris.getCurrentDimension();
    }
    
    /**
     * 检查是否正在进行IP跨维度渲染
     */
    public boolean isInImmersivePortalRendering() {
        return CURRENT_RENDERING_DIMENSION.get() != null;
    }
    
    /**
     * 获取当前渲染深度
     */
    public int getRenderingDepth() {
        return RENDERING_DEPTH.get();
    }
    
    /**
     * 清理线程本地存储（用于调试和测试）
     */
    public void cleanup() {
        CURRENT_RENDERING_DIMENSION.remove();
        RENDERING_DEPTH.remove();
        Iris.logger.debug("IP: Cleaned up dimension context");
    }
    
    /**
     * 获取指定维度的ClientLevel实例
     */
    @Nullable
    public ClientLevel getClientLevel(ResourceKey<Level> dimension) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.dimension().equals(dimension)) {
            return minecraft.level;
        }
        
        // 对于其他维度，我们需要通过其他方式获取
        // 这里可能需要与IP的API集成
        return null;
    }
}