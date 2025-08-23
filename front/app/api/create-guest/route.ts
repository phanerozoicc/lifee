import { userApi } from "@/lib/backend-api"

export async function POST(request: Request) {
  try {
    const { userId } = await request.json()

    if (!userId) {
      return new Response(JSON.stringify({ error: "Missing userId" }), {
        status: 400,
      })
    }

    // 使用后端API创建访客用户
    const response = await userApi.createGuestUser(userId)
    
    if (response.error) {
      console.error("Error creating guest user:", response.error)
      return new Response(
        JSON.stringify({
          error: "Failed to create guest user",
          details: response.error,
        }),
        { status: 500 }
      )
    }

    return new Response(JSON.stringify({ user: response.data }), { status: 200 })
  } catch (err: unknown) {
    console.error("Error in create-guest endpoint:", err)

    return new Response(
      JSON.stringify({ error: (err as Error).message || "Internal server error" }),
      { status: 500 }
    )
  }
}
