"use client"

import { FolderPlusIcon } from "@phosphor-icons/react"
import { useQuery } from "@tanstack/react-query"
import { useState } from "react"
import { DialogCreateProject } from "./dialog-create-project"
import { SidebarProjectItem } from "./sidebar-project-item"

type Project = {
  id: string
  name: string
  user_id: string
  created_at: string
}

export function SidebarProject() {
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  const { data: projects = [], isLoading, error } = useQuery<Project[]>({
    queryKey: ["projects"],
    queryFn: async () => {
      const response = await fetch("/api/projects")
      if (!response.ok) {
        throw new Error("Failed to fetch projects")
      }
      const data = await response.json()
      // Ensure we always return an array
      return Array.isArray(data) ? data : []
    },
    // Provide fallback data to ensure projects is always an array
    initialData: [],
    retry: false,
  })

  return (
    <div className="mb-5">
      <button
        className="hover:bg-accent/80 hover:text-foreground text-primary group/new-chat relative inline-flex w-full items-center rounded-md bg-transparent px-2 py-2 text-sm transition-colors"
        type="button"
        onClick={() => setIsDialogOpen(true)}
      >
        <div className="flex items-center gap-2">
          <FolderPlusIcon size={20} />
          New project
        </div>
      </button>

      {isLoading ? null : (
        <div className="space-y-1">
          {Array.isArray(projects) && projects.length > 0 ? (
            projects.map((project) => (
              <SidebarProjectItem key={project.id} project={project} />
            ))
          ) : error ? (
            <div className="px-2 py-1 text-xs text-muted-foreground">
              Failed to load projects
            </div>
          ) : (
            <div className="px-2 py-1 text-xs text-muted-foreground">
              No projects yet
            </div>
          )}
        </div>
      )}

      <DialogCreateProject isOpen={isDialogOpen} setIsOpen={setIsDialogOpen} />
    </div>
  )
}
