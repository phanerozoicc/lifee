import { redirect } from "next/navigation"

export const dynamic = "force-dynamic"

export default function Home() {
  // Redirect to recommendations page as the new home
  redirect("/recommendations")
}
