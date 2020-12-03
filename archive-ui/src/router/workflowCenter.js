export const workflowCenter = [
  {
    meta: {
      requireAuth: true,
      permit: 1,
      title: 'workflowCenter'
    },
    path: '/workflow/workflowCenter',
    name: '流程中心',
    component: () => import('@/views/workflow/WorkflowCenter.vue'),

  },
  {
    meta: {
      requireAuth: true,
      permit: 1,
      title: 'todoTask'
    },
    path: '/workflow/todoTaskNew',
    name: '代办任务',
    component: () => import('@/views/workflow/task/TodoTaskNew.vue'),
  },
  {
    meta: {
      requireAuth: true,
      permit: 1,
      title: 'doneTask'
    },
    path: '/workflow/doneTaskNew',
    name: '已办任务',
    component: () => import('@/views/workflow/task/DoneTaskNew.vue'),
  },
  {
    meta: {
      requireAuth: true,
      permit: 1,
      title: 'myWorkflow'
    },
    path: '/workflow/MyWorkflowNew',
    name: '我的流程',
    component: () => import('@/views/workflow/task/MyWorkflowNew.vue'),
  },
  {
    meta: {
      requireAuth: true,
      permit: 1,
      title: 'Drafts'
    },
    path: '/workflow/drafts',
    name: '草稿箱',
    component: () => import('@/views/workflow/task/Drafts.vue'),

  }

]








