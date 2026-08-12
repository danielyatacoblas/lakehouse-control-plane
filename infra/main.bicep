param location string = resourceGroup().location
param appName string = 'lakehouse-control-plane'
param image string

resource environment 'Microsoft.App/managedEnvironments@2025-07-01' = {
  name: '${appName}-env'
  location: location
}
resource app 'Microsoft.App/containerApps@2025-07-01' = {
  name: appName
  location: location
  identity: { type: 'SystemAssigned' }
  properties: {
    managedEnvironmentId: environment.id
    configuration: { ingress: { external: true, targetPort: 8098 } }
    template: { containers: [{ name: appName, image: image, resources: { cpu: json('0.5'), memory: '1Gi' } }] }
  }
}
output principalId string = app.identity.principalId
