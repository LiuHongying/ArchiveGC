export const workflowRouter = 
{
  meta: {
    requireAuth: true,
    permit: 1,
    title: 'taskCenter'
  },
  path: '/taskcenter',
  name: '任务中心',
  component: () => import('@/views/TaskCenter.vue'),
  children:
    [
        {
          meta: {
            requireAuth: true,
            permit: 1,
            title: 'todoTask'
          },
          path: '/workflow/todotask',
          name: '待办工作',
          component: () => import('@/views/workflow/task/TodoTask.vue'),
          children:[
            {
              meta: {
                requireAuth: true,
                permit: 1
              },
              path: '/taskTestForm1',
              name: '测试1',
              component: () => import('@/components/form/TaskTestForm1.vue')
              
            }
          ]
    
        },
        {
          meta: {
            requireAuth: true,
            permit: 1,
            title: "doneTask"
          },
          path: '/workflow/donetask',
          name: '已办工作',
          component: () => import('@/views/workflow/DoneTask.vue'),
          children:[
            {
              meta: {
                requireAuth: true,
                permit: 1
              },
              path: '/borrow2',
              name: '测试2',
              component: () => import('@/components/form/Borrow.vue')
              
            }
          ]

        },
        {
          meta: {
            requireAuth: true,
            permit: 1,
            title: 'myWorkflow'
          },
          path: '/workflow/myworkflow',
          name: '我的流程',
          component: () => import('@/views/workflow/MyWorkflow.vue'),
          children:[
            {
              meta: {
                requireAuth: true,
                permit: 1,
              },
              path: '/borrow3',
              name: '测试3',
              component: () => import('@/components/form/Borrow.vue')
              
            }
          ]
        },{
          meta: {
            requireAuth: true,
            permit: 1,
            title: 'waitingBack'
          },
          path: '/record/archiveoutgoing/archivegivebackmine',
          name: '待归还',
          component: () => import('@/views/record/ArchivePendinggivebackMine.vue')
          
        },
        {
          meta: {
              requireAuth: true,
              permit: 1,
              title:'替换文件'
          },
          path: '/workflow/linkMainattachmentfile',
          name: '替换文件',
          component: () => import('@/views/workflow/LinkMainAttachmentFile.vue')
      },
      {
        meta: {
            requireAuth: true,
            permit: 1,
            title:'替换文件'
        },
        path: '/workflow/linkMainAttachmentFileByReviewer',
        name: '审核人修改',
        component: () => import('@/views/workflow/LinkMainAttachmentFileByReviewer.vue')
    },
    {meta:{
      requireAuth: true,
      permit: 1,
      title: 'StartUp'
    },
    path: '/workflow/BorrowStartUp',
    name: '借阅流程启动',
    component: () => import('@/views/workflow/BorrowStartUp.vue')
    
  },
    {meta:{
      requireAuth: true,
      permit: 1,
      title: 'StartUp'
    },
    path: '/workflow/RelyOnFolderSelectStartUp',
    name: '科研预归档借阅流程启动',
    component: () => import('@/views/workflow/RelyOnFolderSelectStartUp.vue')
    
  },
  {meta:{
    requireAuth: true,
    permit: 1,
    title: 'BorrowView'
  },
  path: '/workflow/BorrowView',
  name: '借阅流程可修改视图',
  component: () => import('@/views/workflow/BorrowView.vue')
  
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'BorrowView'
},
path: '/workflow/BorrowViewReadOnly',
name: '借阅流程只读视图',
component: () => import('@/views/workflow/BorrowViewReadOnly.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'CopyStartUp'
},
path: '/workflow/CopyStartUp',
name: '借阅流程只读视图',
component: () => import('@/views/workflow/CopyStartUp.vue')
},

    ]
  }
