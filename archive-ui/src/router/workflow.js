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
    title: 'CommonViewRelyDocType'
  },
  path: '/workflow/CommonViewRelyDocType',
  name: '基于文件类型显示任务组件',
  component: () => import('@/views/workflow/CommonViewRelyDocType.vue')
  
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'CommonViewRelyFolder'
},
path: '/workflow/CommonViewRelyFolder',
name: '基于文件夹路径显示文件列表组件',
component: () => import('@/views/workflow/CommonViewRelyFolder.vue')

},
  {meta:{
    requireAuth: true,
    permit: 1,
    title: 'StartUp'
  },
  path: '/workflow/DesignChangeFileStartup',
  name: '设计文件流程启动',
  component: () => import('@/views/workflow/DesignChangeFileStartup.vue')
  
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
name: '复制流程启动',
component: () => import('@/views/workflow/CopyStartUp.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'CancelStartUp'
},
path: '/workflow/CancelStartUp',
name: '作废通知单作废流程启动',
component: () => import('@/views/workflow/CancelStartUp.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'CancelView'
},
path: '/workflow/CancelView',
name: '作废通知单作废流程视图',
component: () => import('@/views/workflow/CancelView.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'CancelViewReadOnly'
},
path: '/workflow/CancelViewReadOnly',
name: '作废通知单作废流程只读视图',
component: () => import('@/views/workflow/CancelViewReadOnly.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'DesignCancelStartUp'
},
path: '/workflow/DesignCancelStartUp',
name: '设计文件作废审批流程启动',
component: () => import('@/views/workflow/DesignCancelStartUp.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'DesignCancelViewReadOnly'
},
path: '/workflow/DesignCancelViewReadOnly',
name: 'DesignCancelViewReadOnly',
component: () => import('@/views/workflow/DesignCancelViewReadOnly.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'DesignCancelView'
},
path: '/workflow/DesignCancelView',
name: 'DesignCancelView',
component: () => import('@/views/workflow/DesignCancelView.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'AppraisalView'
},
path: '/workflow/AppraisalView',
name: 'AppraisalView',
component: () => import('@/views/workflow/AppraisalView.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'AppraisalViewReadOnly'
},
path: '/workflow/AppraisalViewReadOnly',
name: 'AppraisalViewReadOnly',
component: () => import('@/views/workflow/AppraisalViewReadOnly.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'DestoryViewReadOnly'
},
path: '/workflow/DestoryViewReadOnly',
name: 'DestoryViewReadOnly',
component: () => import('@/views/workflow/DestoryViewReadOnly.vue')
},
{meta:{
  requireAuth: true,
  permit: 1,
  title: 'DestoryStartUp'
},
path: '/workflow/DestoryStartUp',
name: 'DestoryStartUp',
component: () => import('@/views/workflow/DestoryStartUp.vue')
},
    ]
  }
