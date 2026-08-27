package io.github.recrivenvi.ravensmodels.client;

import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

final class MeshUvHandedness {
    private MeshUvHandedness() {
    }

    static void requireVanilla(
            ResourceLocation geometry,
            Vector3f position0, float u0, float v0,
            Vector3f position1, float u1, float v1,
            Vector3f position2, float u2, float v2) {
        Vector3f edge1 = new Vector3f(position1).sub(position0);
        Vector3f edge2 = new Vector3f(position2).sub(position0);
        float du1 = u1 - u0;
        float dv1 = v1 - v0;
        float du2 = u2 - u0;
        float dv2 = v2 - v0;
        float determinant = du1 * dv2 - du2 * dv1;
        if (Math.abs(determinant) < 1.0e-12f) {
            throw new IllegalStateException("Degenerate mesh UV triangle in " + geometry);
        }

        float inverseDeterminant = 1.0f / determinant;
        Vector3f dPdu = new Vector3f(edge1).mul(dv2)
                .sub(new Vector3f(edge2).mul(dv1))
                .mul(inverseDeterminant);
        Vector3f dPdv = new Vector3f(edge1).mul(-du2)
                .add(new Vector3f(edge2).mul(du1))
                .mul(inverseDeterminant);
        Vector3f normal = new Vector3f(edge1).cross(edge2).normalize();
        Vector3f tangent = new Vector3f(dPdu)
                .sub(new Vector3f(normal).mul(normal.dot(dPdu)))
                .normalize();
        Vector3f candidateBitangent = new Vector3f(normal).cross(tangent).normalize();
        if (candidateBitangent.dot(dPdv) >= 0.0f) {
            throw new IllegalStateException(
                    "Mesh UV handedness must match vanilla H=-1 in " + geometry);
        }
    }
}
